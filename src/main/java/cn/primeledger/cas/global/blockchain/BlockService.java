package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.common.entity.BroadcastMessageEntity;
import cn.primeledger.cas.global.common.event.BroadcastEvent;
import cn.primeledger.cas.global.consensus.ScoreManager;
import cn.primeledger.cas.global.consensus.SignBlockScoreStrategy;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static cn.primeledger.cas.global.constants.EntityType.BLOCK_BROADCAST;

/**
 * @author baizhengwen
 * @date 2018/2/23
 */
@Service
@Slf4j
public class BlockService {

    @Resource(name = "blockData")
    private ConcurrentMap<String, Block> blockMap;

    @Resource(name = "blockIndexData")
    private ConcurrentMap<Long, BlockIndex> blockIndexMap;

    @Resource(name = "transactionIndexData")
    private ConcurrentMap<String, TransactionIndex> transactionIndexMap;

    @Resource(name = "utxoData")
    private ConcurrentMap<String, UTXO> utxoMap;

    @Resource(name = "myUTXOData")
    private ConcurrentMap<String, UTXO> myUTXOData;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private BlockCacheManager blockCacheManager;

    @Autowired
    private KeyPair peerKeyPair;

    @Autowired
    private ScoreManager scoreManager;

    /**
     * the max distance that miner got signatures
     */
    private static short MAX_DISTANCE_SIG = 50;

    /**
     * the max tx num in block
     */
    private static short MAX_TX_NUM_IN_BLOCK = 500;

    public boolean initGenesisBlock() {
        Set set = blockMap.keySet();
        if (CollectionUtils.isEmpty(set)) {
            //todo  yuguojia optimize to judge genesis block
            boolean b = genesisBlock();
            return b;
        }
        return true;
    }

    /**
     * 创建创世区块
     *
     * @return
     */
    public boolean genesisBlock() {
//        KeyPair keyPair = new KeyPair("6f297284275fe7d774977dd79d20496b3b8fc0405f64f28033842da74403ecb5",
//                "024d2913d1390e5fcb74567291fe1cb3f7e53bac1fda5703e16df0b9df1fbc5e38");
        //build coin base tx
        ECKey casKey = ECKey.fromPrivateKey("1954b19a2f78e1a1b5a42bdc042e66a671152cc7a3ccab40b1bca14685a6d962");
        List<BaseTx> transactions = Lists.newLinkedList();
        String address = casKey.toBase58Address();
        BaseTx tx = transactionService.buildCoinBasePreMine(new BigDecimal(900), address, 0, (short) 0);
        transactions.add(tx);

        // todo baizhengwen 添加需要写入创世块的交易
        Block block = Block.builder()
                .version((short) 1)
                .blockTime(0)
                .prevBlockHash(null)
                .transactions(transactions)
                .height(1)
                .build();
        String sig = ECKey.signMessage(block.getHash(), peerKeyPair.getPriKey());
        block.initMinerPkSig(peerKeyPair.getPubKey(), sig);
        persistBlockAndIndex(block);
        //donot need broadcast
        return true;
    }

    public Block packageNewBlock() {
        Map<String, BaseTx> txMap = txCacheManager.getTransactionMap();
        if (CollectionUtils.isEmpty(txMap.values())) {
            LOGGER.warn("no transactions for block");
            return null;
        }
        LOGGER.info("try to packageNewBlock");
        //TODO yuguojia collect part txs for package
        List<BaseTx> transactions = Lists.newLinkedList();
        transactions.addAll(txMap.values());

        BlockIndex lastBlockIndex = getLastBlockIndex();
        if (lastBlockIndex == null) {
            throw new RuntimeException("no last block index");
        }
        Block block = Block.builder()
                .version((short) 1)
                .blockTime(0)
                .prevBlockHash(lastBlockIndex.getBestBlockHash())
                .transactions(transactions)
                .height(lastBlockIndex.getHeight() + 1)
                .build();
        String sig = ECKey.signMessage(block.getHash(), peerKeyPair.getPriKey());
        block.initMinerPkSig(peerKeyPair.getPubKey(), sig);
        persistBlockAndIndex(block);
        broadCastBlock(block);
        return block;
    }

    public BlockIndex getLastBlockIndex() {
        long maxHeight = blockIndexMap.keySet().size();
        return blockIndexMap.get(maxHeight);
    }

    public void persistBlockAndIndex(Block block) {
        boolean isBest = true;
        blockMap.put(block.getHash(), block);

        //build block index
        BlockIndex blockIndex;
        ArrayList blockHashs = new ArrayList<String>(1);
        if (block.isgenesisBlock()) {
            blockHashs.add(block.getHash());
            blockIndex = new BlockIndex(1, blockHashs, 0);
        } else {
            boolean needSwitch = needSwitchToBestChain(block);

            blockIndex = blockIndexMap.get(block.getHeight());
            BlockIndex preBlockIndex = blockIndexMap.get(block.getHeight() - 1);
            if (needSwitch) {
                switchToBestChain(preBlockIndex, block.getPrevBlockHash());
            }

            isBest = preBlockIndex == null ? false : preBlockIndex.isBest(block.getPrevBlockHash());

            if (blockIndex == null) {
                blockHashs.add(block.getHash());
                blockIndex = new BlockIndex(block.getHeight(), blockHashs, isBest ? 0 : -1);
            } else {
                blockIndex.addBlockHash(block.getHash(), isBest);
            }
        }
        blockIndexMap.put(blockIndex.getHeight(), blockIndex);

        //build transaction index and utxo
        if (!block.isEmptyTransactions()) {
            List<BaseTx> transactionList = block.getTransactions();
            for (int txCount = 0; txCount < transactionList.size(); txCount++) {
                BaseTx tx = transactionList.get(txCount);
                //remove tx that donot need to be packaged
                txCacheManager.remove(tx.getHash());

                //add new tx index
                TransactionIndex newTxIndex = new TransactionIndex(block.getHash(), tx.getHash(), (short) txCount);
                transactionIndexMap.put(tx.getHash(), newTxIndex);

                if (!(tx instanceof InputOutputTx)) {
                    continue;
                }

                InputOutputTx inputOutputTx = (InputOutputTx) tx;

                List<? extends BaseInput> inputList = inputOutputTx.getInputs();
                if (CollectionUtils.isNotEmpty(inputList)) {
                    for (BaseInput input : inputList) {
                        TxOutPoint outPoint = input.getPrevOut();
                        String spentTxHash = outPoint.getHash();
                        short spentTxOutIndex = outPoint.getIndex();
                        TransactionIndex txIndex = transactionIndexMap.get(spentTxHash);
                        if (txIndex == null) {
                            LOGGER.error("persist block, cannot find tx");
                            return;
                        }
                        txIndex.addSpend(spentTxOutIndex, tx.getHash());
                        transactionIndexMap.put(txIndex.getTxHash(), txIndex);
                        //remove spent utxo
                        utxoMap.remove(UTXO.buildKey(spentTxHash, spentTxOutIndex));
                    }
                }

                //add new utxo
                List<? extends BaseOutput> outputs = inputOutputTx.getOutputs();
                if (CollectionUtils.isNotEmpty(outputs)) {
                    for (int i = 0; i < outputs.size(); i++) {
                        BaseOutput output = outputs.get(i);
                        UTXO utxo = new UTXO(inputOutputTx, (short) i, output);
                        utxoMap.put(utxo.getKey(), utxo);
                        String address = ECKey.pubKey2Base58Address(peerKeyPair.getPubKey());
                        if (StringUtils.equals(utxo.getAddress(), address)) {
                            myUTXOData.put(utxo.getKey(), utxo);
                        }
                    }
                }
            }
        }

        //calc miner score
//        MinerScoreStrategy.refreshMinersScore(block, isBest);
    }

    public void broadCastBlock(Block block) {
        BroadcastMessageEntity entity = new BroadcastMessageEntity();
        entity.setType(BLOCK_BROADCAST.getType());
        entity.setVersion(BLOCK_BROADCAST.getVersion());
        entity.setData(JSON.toJSONString(block));
        Application.EVENT_BUS.post(new BroadcastEvent(entity));

        LOGGER.info("broadcast block success: " + JSON.toJSONString(block));
    }

    /**
     * try to add a new block to db, whether the block adding a old not best chain,
     * but the branch will change to be best chain after added, and the old best chain change to brach.
     *
     * @param block
     * @return
     */
    public boolean needSwitchToBestChain(Block block) {
        BlockIndex blockIndex = blockIndexMap.get(block.getHeight());
        BlockIndex preBlockIndex = blockIndexMap.get(block.getHeight() - 1);
        if (preBlockIndex == null) {
            return false;
        }

        boolean isBestOfPreBlock = preBlockIndex.isBest(block.getPrevBlockHash());

        if (blockIndex != null) {
            //now it is the most longer chain, but its pre block isnot the best chain
            if (!isBestOfPreBlock) {
                return true;
            } else {
                return false;
            }
        } else {

        }
        if (isBestOfPreBlock) {
            return false;
        } else {

        }

        //now it is branch but after the block being added, the branch is the most height
        if (blockIndex != null) {
            //TODO yuguojia the same height, but need to switch to best chain,such as calculating block score
            return false;
        }

        return true;
    }

    public void switchToBestChain(BlockIndex branchBlockIndex, String branchBlockHash) {
        if (!branchBlockIndex.valid()) {
            return;
        }

        boolean isBest = branchBlockIndex.isBest(branchBlockHash);
        if (isBest) {
            // it is now best, cannot switch,stop to switch
            return;
        }
        boolean success = branchBlockIndex.switchToBestChain(branchBlockHash);
        if (!success) {
            return;
        }
        Block block = blockMap.get(branchBlockHash);
        BlockIndex preBlockIndex = blockIndexMap.get(branchBlockIndex.getHeight() - 1);
        //recursion switch to best chain
        switchToBestChain(preBlockIndex, block.getPrevBlockHash());
    }

    public void loadAllBlockData() {
        Iterator<String> blockIterator = blockMap.keySet().iterator();
        Iterator<Long> blockIndexIt = blockIndexMap.keySet().iterator();
        while (blockIndexIt.hasNext()) {
            Long next = blockIndexIt.next();
            BlockIndex blockIndex = blockIndexMap.get(next);
        }
        while (blockIterator.hasNext()) {
            String next = blockIterator.next();
            Block block = blockMap.get(next);
        }
    }

    public int calcSignaturesScore(Block block) {
        int sigScore = 0;
        List<PubKeyAndSignaturePair> otherPKSigs = block.getOtherPKSigs();
        for (PubKeyAndSignaturePair pair : otherPKSigs) {
            String sigBlockHash = pair.getBlockHash();
            Block sigBlock = blockMap.get(sigBlockHash);
            int score = SignBlockScoreStrategy.calcSignScore(block, sigBlock);
            sigScore += score;
        }
        return sigScore;
    }

    public void printAllBlockData() {
        int max_num = 500;
        int count = 0;
        Iterator<String> blockIterator = blockMap.keySet().iterator();
        Iterator<Long> blockIndexIterator = blockIndexMap.keySet().iterator();
        Iterator<String> txIndexIterator = transactionIndexMap.keySet().iterator();
        Iterator<String> utxoIterator = utxoMap.keySet().iterator();
        StringBuilder sb = new StringBuilder(1024);
        while (blockIterator.hasNext() && count++ <= max_num) {
            String next = blockIterator.next();
            Block block = blockMap.get(next);
            LOGGER.info("block info: " + block);
        }
        count = 0;
        while (blockIndexIterator.hasNext() && count++ <= max_num) {
            Long next = blockIndexIterator.next();
            BlockIndex blockIndex = blockIndexMap.get(next);
            LOGGER.info("block index info: " + blockIndex);
        }
        count = 0;
        while (txIndexIterator.hasNext() && count++ <= max_num) {
            String next = txIndexIterator.next();
            TransactionIndex transactionIndex = transactionIndexMap.get(next);
            LOGGER.info("tx index info: " + transactionIndex);
        }
        count = 0;
        while (utxoIterator.hasNext() && count++ <= max_num) {
            String next = utxoIterator.next();
            UTXO utxo = utxoMap.get(next);
            LOGGER.info("utxo info: " + utxo);
        }
        LOGGER.info("my key pair info: " + peerKeyPair);
    }

    public boolean isExit(Block block) {
        if (blockMap.get(block.getHash()) != null ||
                blockCacheManager.isContains(block.getHash())) {
            return true;
        }
        return false;
    }

    public boolean preIsExit(Block block) {
        if (preIsExitInDB(block) || preIsExitInCache(block)) {
            return true;
        }
        return false;
    }

    public boolean preIsExitInDB(Block block) {
        if (blockMap.get(block.getPrevBlockHash()) != null) {
            return true;
        }
        return false;
    }

    public boolean preIsExitInCache(Block block) {
        if (blockCacheManager.isContains(block.getPrevBlockHash())) {
            return true;
        }
        return false;
    }

    public boolean valid(Block block) {
        short version = block.getVersion();
        long height = block.getHeight();
        String blockHash = block.getHash();
        String prevBlockHash = block.getPrevBlockHash();
        List<BaseTx> transactions = block.getTransactions();
        block.getPubKeyAndSignaturePairs();

        // check block struct
        if (version < 0 ||
                height < 1 ||
                StringUtils.isEmpty(prevBlockHash) ||
                CollectionUtils.isEmpty(transactions)) {
            return false;
        }
        if (isExit(block)) {
            //has existed same block
            return false;
        }
        if (!preIsExitInDB(block)) {
            //the pre block not exit in db
            //todo yuguojia exit in cache
            return false;
        }

        // check signature
        PubKeyAndSignaturePair minerPKSig = block.getMinerPKSig();
        List<PubKeyAndSignaturePair> otherPKSigs = block.getOtherPKSigs();
        if (minerPKSig == null) {
            // TODO:  yuguojia CollectionUtils.isEmpty(otherPKSigs)
            return false;
        }
        if (!minerPKSig.valid(true) ||
                !ECKey.verifySign(blockHash, minerPKSig.getSignature(), minerPKSig.getPubKey())) {
            return false;
        }

        Set<String> pkSet = new HashSet<>();
        for (PubKeyAndSignaturePair pair : otherPKSigs) {
            pkSet.add(pair.getPubKey());
            if (!pair.valid(false) ||
                    !ECKey.verifySign(blockHash, pair.getSignature(), pair.getPubKey())) {
                return false;
            }
            Block preBlock = blockMap.get(pair.getBlockHash());
            if (preBlock == null) {
                return false;
            }
            if (!StringUtils.equals(preBlock.getMinerPKSig().getPubKey(), pair.getPubKey())) {
                return false;
            }
            if (block.getHeight() - preBlock.getHeight() > MAX_DISTANCE_SIG) {
                return false;
            }
        }
        if (pkSet.size() != otherPKSigs.size()) {
            //there are duplicate pks
            return false;
        }

        // check transactions
        for (BaseTx tx : transactions) {
            if (!transactionService.valid(tx)) {
                return false;
            }
        }

        //TODO: yuguojia score

        return true;
    }
}
