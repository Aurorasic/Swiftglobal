package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.common.entity.BroadcastMessageEntity;
import cn.primeledger.cas.global.common.event.BroadcastEvent;
import cn.primeledger.cas.global.consensus.*;
import cn.primeledger.cas.global.consensus.sign.service.CollectSignService;
import cn.primeledger.cas.global.consensus.syncblock.SyncBlockService;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    private static final int LRU_CACHE_SIZE = 5;

    private static List<String> COMMUN_ADDRS = new ArrayList<String>();

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

    @Autowired
    private NodeSelector nodeSelector;

    @Autowired
    private CollectSignService collectSignService;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private SyncBlockService syncBlockService;

    @Autowired
    private PreMiningService preMiningService;

    /**
     * the max distance that miner got signatures
     */
    private static short MAX_DISTANCE_SIG = 50;


    /**
     * the max tx num in block
     */
    private static short MAX_TX_NUM_IN_BLOCK = 500;

    private Cache<String, Block> blockCache = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE).build();

    public boolean initGenesisBlock() {
        return preMiningService.initGenesisBlocks();
    }

    public Block packageNewBlock() {
        Map<String, Transaction> txMap = txCacheManager.getTransactionMap();
        if (CollectionUtils.isEmpty(txMap.values())) {
            LOGGER.warn("no transactions for block");
            return null;
        }
        LOGGER.info("try to packageNewBlock");
        //TODO yuguojia collect part txs for package
        List<Transaction> transactions = Lists.newLinkedList();
        transactions.addAll(txMap.values());

        BlockIndex lastBlockIndex = getLastBestBlockIndex();
        if (lastBlockIndex == null) {
            throw new RuntimeException("no last block index");
        }
        List<String> nodes = nodeSelector.calculateNodes();
        Block block = Block.builder()
                .version((short) 1)
                .blockTime(0)
                .prevBlockHash(lastBlockIndex.getBestBlockHash())
                .transactions(transactions)
                .height(lastBlockIndex.getHeight() + 1)
                .nodes(nodes)
                .build();

        //Before collecting signs from witnesses just cache the block firstly.
        String sig = ECKey.signMessage(block.getHash(), peerKeyPair.getPriKey());
        block.initMinerPkSig(peerKeyPair.getPubKey(), sig);
        blockCache.put(block.getHash(), block);
        return block;
    }

    public Block getLocalBlock(String key) {
        return blockCache.getIfPresent(key);
    }

    public void removeLocalBlock(String key) {
        blockCache.invalidate(key);
    }

    public BlockIndex getLastBlockIndex() {
        long maxHeight = blockIndexMap.keySet().size();
        return blockIndexMap.get(maxHeight);
    }

    public BlockIndex getLastBestBlockIndex() {
        BlockIndex lastBlockIndex = getLastBlockIndex();
        if (lastBlockIndex == null) {
            return null;
        }
        long lastHeight = lastBlockIndex.getHeight();
        String bestBlockHash = lastBlockIndex.getBestBlockHash();
        if (StringUtils.isNotBlank(bestBlockHash)) {
            return lastBlockIndex;
        }
        while ((lastBlockIndex = blockIndexMap.get(--lastHeight)) != null) {
            bestBlockHash = lastBlockIndex.getBestBlockHash();
            if (StringUtils.isNotBlank(bestBlockHash)) {
                return lastBlockIndex;
            }
        }
        return null;
    }

    public long getMaxHeight() {
        BlockIndex lastBestBlockIndex = getLastBestBlockIndex();
        if (lastBestBlockIndex == null) {
            return 0L;
        }
        return lastBestBlockIndex.getHeight();
    }

    public Block getLastBestBlock() {
        BlockIndex lastBlockIndex = getLastBestBlockIndex();
        if (lastBlockIndex == null) {
            return null;
        }
        String blockHash = lastBlockIndex.getBestBlockHash();
        return blockMap.get(blockHash);
    }

    /**
     * persist the block and its index data. If it is orphan block, add it to cache and do not persist to db
     *
     * @param block
     */
    public boolean persistBlockAndIndex(Block block, String sourceId, short version) {
        String bestBlockHash = null;
        Block highestScoreBlock = null;
        BlockFullInfo blockFullInfo = new BlockFullInfo(version, sourceId, block);
        if (isExistInDB(block.getHash())) {
            return false;
        }
        if (hasBestBlock(block.getHeight())) {
            LOGGER.info("there has best block on height={}, do not persist block:{}", block.getHeight(), block.getHash());
            return false;
        }
        //orphan block, add cache and return, do not persist to db
        if (!preIsExitInDB(block) && !block.isgenesisBlock()) {
            blockCacheManager.put(blockFullInfo);
            return true;
        }

        if (block.isPreBlock()) {
            bestBlockHash = block.getHash();
            highestScoreBlock = block;
        } else {
            List<Block> sameHeightBlocks = getBlocksByHeight(block.getHeight());
            sameHeightBlocks.add(block);
            highestScoreBlock = getHighestScoreBlock(sameHeightBlocks);
            if (highestScoreBlock != null) {
                bestBlockHash = highestScoreBlock.getHash();
            }
        }
        LOGGER.info("got highest score block: height={}, blockHash={}", block.getHeight(), bestBlockHash);

        blockMap.put(block.getHash(), block);
        LOGGER.info("persisted block, height={}, hash={}", block.getHeight(), block.getHash());

        //persist block and tx and utxo index
        persistIndex(block, bestBlockHash);

        //calc dpos node group
        nodeManager.parse(highestScoreBlock);

        //calc miner score
        MinerScoreStrategy.refreshMinersScore(highestScoreBlock);

        //persist pre-block in orphan block cache
        persistPreOrphanBlock(blockFullInfo);
        return true;
    }

    public void persistIndex(Block block, String bestBlockHash) {

        boolean needBuildUTXO = false;
        //build block index
        BlockIndex blockIndex;
        ArrayList blockHashs = new ArrayList<String>(1);
        if (block.isgenesisBlock()) {
            blockHashs.add(block.getHash());
            blockIndex = new BlockIndex(1, blockHashs, 0);
            needBuildUTXO = true;
        } else {
//            boolean needSwitch = needSwitchToBestChain(block);
            blockIndex = blockIndexMap.get(block.getHeight());
            boolean hasOldBest = blockIndex == null ? false : blockIndex.hasBestBlock();
//            BlockIndex preBlockIndex = blockIndexMap.get(block.getHeight() - 1);
//            if (needSwitch) {
//                switchToBestChain(preBlockIndex, block.getPrevBlockHash());
//            }

            boolean isBest = StringUtils.equals(bestBlockHash, block.getHash()) ? true : false;

            if (blockIndex == null) {
                blockHashs.add(block.getHash());
                blockIndex = new BlockIndex(block.getHeight(), blockHashs, isBest ? 0 : -1);
            } else {
                blockIndex.addBlockHash(block.getHash(), isBest);
                blockIndex.setBestHash(bestBlockHash);
            }

            boolean hasNewBest = blockIndex.hasBestBlock();
            needBuildUTXO = !hasOldBest && hasNewBest;
        }
        blockIndexMap.put(blockIndex.getHeight(), blockIndex);
        LOGGER.info("persisted block index: " + blockIndex.toString());

        //build transaction index and utxo
        if (!needBuildUTXO) {
            return;
        }
        Block bestBlock = getBlock(bestBlockHash);
        if (!bestBlock.isEmptyTransactions()) {
            List<Transaction> transactionList = bestBlock.getTransactions();
            for (int txCount = 0; txCount < transactionList.size(); txCount++) {
                Transaction tx = transactionList.get(txCount);
                //remove tx that donot need to be packaged
                txCacheManager.remove(tx.getHash());

                //add new tx index
                TransactionIndex newTxIndex = new TransactionIndex(bestBlock.getHash(), tx.getHash(), (short) txCount);
                transactionIndexMap.put(tx.getHash(), newTxIndex);

                List<TransactionInput> inputList = tx.getInputs();
                if (CollectionUtils.isNotEmpty(inputList)) {
                    for (TransactionInput input : inputList) {
                        TransactionOutPoint outPoint = input.getPrevOut();
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
                List<TransactionOutput> outputs = tx.getOutputs();
                if (CollectionUtils.isNotEmpty(outputs)) {
                    for (int i = 0; i < outputs.size(); i++) {
                        TransactionOutput output = outputs.get(i);
                        UTXO utxo = new UTXO(tx, (short) i, output);
                        utxoMap.put(utxo.getKey(), utxo);
                        String address = ECKey.pubKey2Base58Address(peerKeyPair.getPubKey());
                        if (StringUtils.equals(utxo.getAddress(), address)) {
                            myUTXOData.put(utxo.getKey(), utxo);
                        }
                    }
                }
            }
        }
    }

    public void broadCastBlock(Block block) {
        BroadcastMessageEntity entity = new BroadcastMessageEntity();
        entity.setType(BLOCK_BROADCAST.getCode());
        entity.setVersion(block.getVersion());
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
        if (blockIndex == null) {
            if (!isBestOfPreBlock) {
                //its parent is not best, but it is the most height block
                return true;
            }
        } else {
            int currentBlockSigsScore = calcSignaturesScore(block);
            List<Block> sameHeightOtherBlocks = getBlocksByHeightExclude(block.getHeight(), block.getHash());
            for (Block brotherBlock : sameHeightOtherBlocks) {
                int brotherBlockSigsScore = calcSignaturesScore(brotherBlock);
                if (currentBlockSigsScore > brotherBlockSigsScore &&
                        isBestOfPreBlock) {
                    //todo yuguojia donot base on height, base on sign score
                    return true;
                }
            }
        }

        return false;
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
        Block oldBestBlock = blockMap.get(branchBlockIndex.getBestBlockHash());
        boolean success = branchBlockIndex.switchToBestChain(branchBlockHash);
        if (!success) {
            return;
        }
        Block newBestBlock = blockMap.get(branchBlockHash);
        MinerScoreStrategy.changeScore(oldBestBlock, newBestBlock);
        BlockIndex preBlockIndex = blockIndexMap.get(branchBlockIndex.getHeight() - 1);
        //recursion switch to best chain
        switchToBestChain(preBlockIndex, newBestBlock.getPrevBlockHash());
    }

    public void loadAllBlockData() {
        //todo yuguojia valid all block data
        long maxHeight = blockIndexMap.keySet().size();
//        long lastDposCalcHeight = ((maxHeight - 1) / NodeSelector.BATCHBLOCKNUM) * NodeSelector.BATCHBLOCKNUM + 1;
        List<String> allBlockHashList = new LinkedList<>();
        Set<String> bestBlockHashSet = new HashSet<>();
        for (long i = 1; i <= maxHeight; i++) {
            BlockIndex blockIndex = blockIndexMap.get(i);
            String bestBlockHash = blockIndex.getBestBlockHash();
            if (StringUtils.isNotEmpty(bestBlockHash)) {
                bestBlockHashSet.add(bestBlockHash);
            }
            allBlockHashList.addAll(blockIndex.getBlockHashs());

        }
        clearAllIndexData();
        for (String blockHash : allBlockHashList) {
            Block block = blockMap.get(blockHash);
            boolean isBest = bestBlockHashSet.contains(blockHash);
            String bestBlockHash = null;
            if (isBest) {
                bestBlockHash = blockHash;
            }
            persistIndex(block, bestBlockHash);
            //calc miner score
            MinerScoreStrategy.refreshMinersScore(block, isBest);
            //calc dpos node group
//            long blockHeight = block.getHeight();
//            if (isBest && lastDposCalcHeight == blockHeight) {
//                nodeManager.parse(block);
//            }
        }

//        MinerScoreStrategy.refreshMinersScore(block, isBest);
//        Block lastBestBlock = getLastBestBlock();
//        parseNextDposNode(lastBestBlock);
    }

    public void parseNextDposNode(Block block) {
        if (block == null) {
            LOGGER.error("error for parseNextDposNode");
            return;
        }
        boolean isSuccess = nodeManager.parse(block);
        if (!isSuccess) {
            if (StringUtils.isEmpty(block.getPrevBlockHash())) {
                LOGGER.error("error for parseNextDposNode, current block: " + block.getHeight());
                return;
            }
            Block preBlock = blockMap.get(block.getPrevBlockHash());
            parseNextDposNode(preBlock);
        }
    }

    public void clearAllIndexData() {
        blockIndexMap.clear();
        transactionIndexMap.clear();
        utxoMap.clear();
        myUTXOData.clear();
    }

    public void clearAllDBData() {
        blockMap.clear();
        blockIndexMap.clear();
        transactionIndexMap.clear();
        utxoMap.clear();
        myUTXOData.clear();
    }

    public boolean hasBestBlock(long height) {
        Block block = getBestBlockByHeight(height);
        return block == null ? false : true;
    }

    public Block getBestBlockByHeight(long height) {
        BlockIndex blockIndex = blockIndexMap.get(height);
        if (blockIndex == null) {
            return null;
        }
        String bestBlockHash = blockIndex.getBestBlockHash();
        if (StringUtils.isEmpty(bestBlockHash)) {
            return null;
        }
        Block block = blockMap.get(bestBlockHash);
        return block;
    }

    public BlockIndex getBlockIndexByHeight(long height) {
        return blockIndexMap.get(height);
    }

    public List<Block> getBlocksByHeight(long height) {
        BlockIndex blockIndex = blockIndexMap.get(height);
        if (null == blockIndex) {
            return new ArrayList<>();
        }
        ArrayList<String> blockHashs = blockIndex.getBlockHashs();
        List<Block> blocks = new LinkedList<>();
        blockHashs.forEach(blockHash -> {
            Block otherBlock = blockMap.get(blockHash);
            if (otherBlock == null) {
                throw new RuntimeException("error for getBlocksByHeight");
            }
            blocks.add(otherBlock);
        });
        return blocks;
    }

    public List<Block> getBlocksByHeightExclude(long height, String excludeBlockHash) {
        BlockIndex blockIndex = blockIndexMap.get(height);
        ArrayList<String> blockHashs = blockIndex.getBlockHashs();
        List<Block> blocks = new LinkedList<>();
        for (String blockHash : blockHashs) {
            if (StringUtils.equals(blockHash, excludeBlockHash)) {
                continue;
            }
            Block otherBlock = blockMap.get(blockHash);
            if (otherBlock == null) {
                throw new RuntimeException("error for getBlocksByHeight");
            }
            blocks.add(otherBlock);
        }
        return blocks;
    }

    public Block getBlock(String blockHash) {
        if (StringUtils.isEmpty(blockHash)) {
            return null;
        }
        return blockMap.get(blockHash);
    }


    public int calcSignaturesScore(Block block) {
        int sigScore = 0;
        List<BlockWitness> otherPKSigs = block.getBlockWitnesses();
        for (BlockWitness pair : otherPKSigs) {
            String sigBlockHash = pair.getBlockHash();
            Block sigBlock = blockMap.get(sigBlockHash);
            int score = SignBlockScoreStrategy.calcSignScore(block, sigBlock);
            sigScore += score;
        }
        LOGGER.info("calculated block({}) , all sign scores is {}", block.getHash(), sigScore);
        return sigScore;
    }

    public Block getHighestScoreBlock(List<Block> blockList) {
        if (CollectionUtils.isEmpty(blockList)) {
            return null;
        }

        boolean canBigger = canMineBiggerScoreBlock(blockList);
        if (!canBigger) {
            Block highestScoreBlock = null;
            long height = blockList.get(0).getHeight();
            int signedMaxScore = Integer.MIN_VALUE;
            for (Block block : blockList) {
                if (height != block.getHeight()) {
                    throw new RuntimeException("there is different block height");
                }
                int score = calcSignaturesScore(block);
                if (score > signedMaxScore) {
                    signedMaxScore = score;
                    highestScoreBlock = block;
                }
            }
            return highestScoreBlock;
        }
        return null;
    }

    public boolean canMineBiggerScoreBlock(List<Block> blockList) {
        if (CollectionUtils.isEmpty(blockList)) {
            return true;
        }
        boolean result = false;
        int signedMaxScore = Integer.MIN_VALUE;
        List<Integer> signedGapSet = new LinkedList<>();
        Set<Integer> trySignGapSet = new HashSet<>();
        long height = blockList.get(0).getHeight();
        for (Block block : blockList) {
            if (height != block.getHeight()) {
                throw new RuntimeException("there is different block height");
            }

            int score = calcSignaturesScore(block);
            if (score > signedMaxScore) {
                signedMaxScore = score;
            }
            List<String> witnessBlockHashList = block.getWitnessBlockHashList();
            for (String witnessBlockHash : witnessBlockHashList) {
                Block witnessBlock = getBlock(witnessBlockHash);
                if (witnessBlock != null) {
                    int gap = (int) (height - witnessBlock.getHeight());
                    signedGapSet.add(gap);
                }
            }
        }
        LOGGER.info("the height={} signed high gaps is {}, and max score is {}", height, signedGapSet, signedMaxScore);

        int mayMaxScore = 0;
        int count = 0;
        Set<Integer> validateSignHeightGap = collectSignService.getValidateSignHeightGap(height);
        for (int gap = 1; gap < SignBlockScoreStrategy.MAX_HIGH_GAP; gap++) {
            if (signedGapSet.contains(gap) || !validateSignHeightGap.contains(gap)) {
                continue;
            }
            trySignGapSet.add(gap);
            mayMaxScore += SignBlockScoreStrategy.getScoreByHighGap(gap);
            count++;
            if (count == CollectSignService.witnessNum) {
                break;
            }
        }
        result = mayMaxScore > signedMaxScore ? true : false;
        LOGGER.info("could collect more high signer score gaps {} for height={}, would be max? {}, score is {}", trySignGapSet, height, result, mayMaxScore);
        return result;
    }

    public void printAllBlockData() {
        int max_num = 100;
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
//        count = 0;
//        while (txIndexIterator.hasNext() && count++ <= max_num) {
//            String next = txIndexIterator.next();
//            TransactionIndex transactionIndex = transactionIndexMap.get(next);
//            LOGGER.info("tx index info: " + transactionIndex);
//        }
//        count = 0;
//        while (utxoIterator.hasNext() && count++ <= max_num) {
//            String next = utxoIterator.next();
//            UTXO utxo = utxoMap.get(next);
//            LOGGER.info("utxo info: " + utxo);
//        }
        LOGGER.info("my key pair info: " + peerKeyPair);
    }

    public boolean isExistInDB(String blockHash) {
        if (blockMap.get(blockHash) != null) {
            return true;
        }
        return false;
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

    public boolean validBlockTransactions(Block block) {
        LOGGER.info("begin to check the transactions of block {}", block.getHeight());

        // check transactions
        for (Transaction tx : block.getTransactions()) {
            if (!transactionService.valid(tx)) {
                return false;
            }
        }
        LOGGER.info("check the transactions success of block {}", block.getHeight());
        return true;
    }

    public boolean validBasic(Block block) {
        short version = block.getVersion();
        String blockHash = block.getHash();
        String signedHash = block.getSignedHash();
        String prevBlockHash = block.getPrevBlockHash();
        List<Transaction> transactions = block.getTransactions();

        if (version < 0 || CollectionUtils.isEmpty(transactions)) {
            return false;
        }
        if (!block.isgenesisBlock() && StringUtils.isEmpty(prevBlockHash)) {
            return false;
        }
        if (isExistInDB(blockHash)) {
            LOGGER.error("the block is exist in db");
            //has existed same block in db
            return false;
        }

        // check signature
        BlockWitness minerPKSig = block.getMinerFirstPKSig();

        if (minerPKSig == null) {
            LOGGER.error("the first miner sign is empty");
            return false;
        }
        if ((!minerPKSig.valid() ||
                !ECKey.verifySign(blockHash, minerPKSig.getSignature(), minerPKSig.getPubKey()))) {
            LOGGER.error("the first miner sign is not valid");
            return false;
        }

        BlockWitness signedWitness = block.getMinerSecondPKSig();
        if (Application.PRE_BLOCK_COUNT < block.getHeight()) {
            LOGGER.info("the block to check is {}", block);
            if (signedWitness == null) {
                LOGGER.error("the signedWitness is empty");
                return false;
            }
            if (!signedWitness.valid()) {
                LOGGER.error("the signedWitness is not valid");
                return false;
            }
            if (!ECKey.verifySign(signedHash, signedWitness.getSignature(), signedWitness.getPubKey())) {
                LOGGER.error("can not verify the witness sign,the signedHash is {} and the signedWitness is {}", signedHash, signedWitness);
                return false;
            }
        }

        List<BlockWitness> otherPKSigs = block.getBlockWitnesses();
        if (Application.PRE_BLOCK_COUNT >= block.getHeight()) {
            return true;
        }

//        if (CollectionUtils.isEmpty(otherPKSigs) || otherPKSigs.size() != 3) {
//            LOGGER.error("the witness is empty or the number of witness is not three");
//            return false;
//        }
        if (CollectionUtils.isEmpty(otherPKSigs)) {
            return true;
        }
        Set<String> pkSet = new HashSet<>();
        for (BlockWitness pair : otherPKSigs) {
            pkSet.add(pair.getPubKey());
            if (!pair.valid() ||
                    !ECKey.verifySign(blockHash, pair.getSignature(), pair.getPubKey())) {
                return false;
            }
            Block witnessBlock = blockMap.get(pair.getBlockHash());
            if (witnessBlock == null) {
                return false;
            }
            if (!StringUtils.equals(witnessBlock.getMinerFirstPKSig().getPubKey(), pair.getPubKey())) {
                return false;
            }
            if (block.getHeight() - witnessBlock.getHeight() > MAX_DISTANCE_SIG) {
                return false;
            }
        }
        if (pkSet.size() != otherPKSigs.size()) {
            //there are duplicate pks
            return false;
        }
        return true;
    }

    public void persistPreOrphanBlock(BlockFullInfo blockFullInfo) {
        Block block = blockFullInfo.getBlock();
        List<BlockFullInfo> nextConnectionBlocks = blockCacheManager.getNextConnectionBlocks(block.getHash());
        if (CollectionUtils.isNotEmpty(nextConnectionBlocks)) {
            for (BlockFullInfo nextBlockFullInfo : nextConnectionBlocks) {
                Block nextBlock = nextBlockFullInfo.getBlock();
                String nextSourceId = nextBlockFullInfo.getSourceId();
                short nextVersion = nextBlockFullInfo.getVersion();
                persistBlockAndIndex(nextBlock, nextSourceId, nextVersion);
                blockCacheManager.remove(nextBlock.getHash());
            }
        }
    }

    public boolean validateWitness(String key, String pubKey, String signature, Block localBlock) {

        if (!localBlock.getHash().equals(key)) {
            LOGGER.error("Invalid block hash from witness");
            return false;
        }

        if (!(validateWitnessParams(key, pubKey, signature))) {
            LOGGER.error("Invalid parameters from witness");
            return false;
        }

        if (!ECKey.verifySign(key, signature, pubKey)) {
            LOGGER.error("Invalid sign from witness");
            return false;
        }

        return true;
    }

    private boolean validateWitnessParams(String key, String pubKey, String signature) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(pubKey) || StringUtils.isEmpty(signature)) {
            return false;
        }

        return true;
    }

//    private boolean validateWitnessFrom(Block localBlock, String pubKey) {
//        boolean exists = false;
//
//        for (BlockWitness witness : localBlock.getBlockWitnesses()) {
//            if (pubKey.equals(witness.getPubKey())) {
//                exists = true;
//                break;
//            }
//        }
//
//        return exists;
//    }

    public boolean validBlockFromProducer(Block data) {

        if (data == null) {
            return false;
        }

        if (data.getVersion() < 0 ||
                data.getHeight() < 1 ||
                StringUtils.isEmpty(data.getPrevBlockHash()) ||
                CollectionUtils.isEmpty(data.getTransactions())) {
            return false;
        }

        if (isExit(data)) {
            LOGGER.error("The block exists in the cache");
            return false;
        }

        // check creator's signature
        if (!validIfFromCreator(data)) {
            LOGGER.error("Validate the block if from creator failed");
            return false;
        }

        if (!validBlockTransactions(data)) {
            LOGGER.error("Validate the transactions");
            return false;
        }

        LOGGER.info("Successfully validate block from creator");
        return true;
    }

    private boolean validateBlockParams(Block data) {
        if (data == null) {
            return false;
        }

        if (data.getVersion() < 0 ||
                data.getHeight() < 1 ||
                StringUtils.isEmpty(data.getPrevBlockHash()) ||
                CollectionUtils.isEmpty(data.getTransactions())) {
            return false;
        }

        List<BlockWitness> blockWitnessList = data.getBlockWitnesses();
        if (CollectionUtils.isEmpty(blockWitnessList)) {
            return false;
        }

        return true;
    }

    private boolean validIfFromCreator(Block block) {
        BlockWitness blockWitness = block.getMinerFirstPKSig();

        if (!nodeManager.checkProducer(blockWitness.getPubKey())) {
            return false;
        }

        if (!ECKey.verifySign(block.getHash(),
                blockWitness.getSignature(),
                blockWitness.getPubKey())) {
            return false;
        }

        return true;
    }

    private boolean validatePreBlock(Block block) {
        if (!preIsExitInDB(block)) {
            //the pre block not exit in db
            //todo yuguojia exit in cache
            return false;
        }

        BlockWitness blockWitness = block.getMinerFirstPKSig();

        Block witnessBlock = blockMap.get(blockWitness.getBlockHash());
        if (witnessBlock == null) {
            return false;
        }

        if (!StringUtils.equals(witnessBlock.getMinerFirstPKSig().getPubKey(), blockWitness.getPubKey())) {
            return false;
        }
        if (block.getHeight() - witnessBlock.getHeight() > MAX_DISTANCE_SIG) {
            return false;
        }

        return true;
    }

    public List<Block> getBestBatchBlocks(long height) {
        List<Block> blocks = new LinkedList<>();
        long fromHeight = (height + 1) / 3 * 3 - 1;
        long endHeight = (height + 1) / 3 * 3 + 1;
        while (fromHeight <= endHeight) {
            Block block = getBestBlockByHeight(fromHeight);
            fromHeight++;
            if (block == null) {
                continue;
            }
            blocks.add(block);
        }
        return blocks;
    }
}
