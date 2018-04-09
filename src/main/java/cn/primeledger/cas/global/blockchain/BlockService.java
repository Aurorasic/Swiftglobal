package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.blockchain.transaction.*;
import cn.primeledger.cas.global.common.SystemStatusManager;
import cn.primeledger.cas.global.common.SystemStepEnum;
import cn.primeledger.cas.global.common.event.BlockPersistedEvent;
import cn.primeledger.cas.global.consensus.*;
import cn.primeledger.cas.global.consensus.sign.service.CollectSignService;
import cn.primeledger.cas.global.consensus.syncblock.SyncBlockService;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.script.UnLockScript;
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

/**
 * @author baizhengwen
 * @date 2018/2/23
 */
@Service
@Slf4j
public class BlockService {

    private static final int LRU_CACHE_SIZE = 5;
    /**
     * the minimum of transactions number allowed in a block.
     */
    private static final int MINIMUM_TRANSACTION_IN_BLOCK = 2;

    private static List<String> COMMUN_ADDRS = new ArrayList<String>();
    /**
     * the max distance that miner got signatures
     */
    private static short MAX_DISTANCE_SIG = 50;
    /**
     * the max tx num in block
     */
    private static short MAX_TX_NUM_IN_BLOCK = 500;
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
    @Resource(name = "pubKeyMap")
    private ConcurrentMap<byte[], byte[]> pubKeyMap;
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
    private MessageCenter messageCenter;
    @Autowired
    private SystemStatusManager systemStatusManager;
    private Cache<String, Block> blockCache = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE).build();

    public Block packageNewBlock(KeyPair keyPair) {
        BlockIndex lastBlockIndex = getLastBestBlockIndex();
        if (lastBlockIndex == null) {
            throw new RuntimeException("no last block index");
        }
        Collection<Transaction> cacheTmpTransactions = txCacheManager.getTransactionMap().asMap().values();
        ArrayList cacheTransactions = new ArrayList(cacheTmpTransactions);
        removeDoubleSpendTx(cacheTransactions);

        if (cacheTransactions.size() < MINIMUM_TRANSACTION_IN_BLOCK - 1) {
            LOGGER.warn("There are no enough transactions, less than two, for packaging a block.");
            return null;
        }
        LOGGER.info("try to packageNewBlock, height={}", lastBlockIndex.getHeight() + 1);
        List<Transaction> transactions = Lists.newLinkedList();
        //todo kongyu add 2018-3-29 21:02
        if (getLastBestBlockIndex().getHeight() >= 1) {
            Transaction coinBaseTx = transactionService.buildCoinBaseTx(0L, (short) 1, lastBlockIndex.getHeight() + 1);
            transactions.add(coinBaseTx);
        }

        transactions.addAll(cacheTransactions);

        List<String> nodes = nodeSelector.calculateNodes(lastBlockIndex.getHeight());
        Block block = new Block();
        block.setVersion((short) 1);
        block.setBlockTime(System.currentTimeMillis());
        block.setPrevBlockHash(lastBlockIndex.getBestBlockHash());
        block.setTransactions(transactions);
        block.setHeight(lastBlockIndex.getHeight() + 1);
        block.setNodes(nodes);
        block.setPubKey(keyPair.getPubKey());
        while (!block.sizeAllowed()) {
            txCacheManager.addTransaction(transactions.remove(transactions.size() - 1));
            if (transactions.size() < MINIMUM_TRANSACTION_IN_BLOCK) {
                LOGGER.warn("The number of transactions available for being packaged into a block is less than two.");
                return null;
            }
        }

        //Before collecting signs from witnesses just cache the block firstly.
        String sig = ECKey.signMessage(block.getHash(), keyPair.getPriKey());
        block.initMinerPkSig(keyPair.getPubKey(), sig);
        blockCache.put(block.getHash(), block);

        //todo test 2018-3-29 21:02
//        boolean isSuccess = transactionService.validCoinBaseTx(coinBaseTx,block);

        return block;
    }

    private void removeDoubleSpendTx(List<Transaction> cacheTransactions) {
        if (CollectionUtils.isEmpty(cacheTransactions)) {
            return;
        }

        HashMap<String, String> spentUTXOMap = new HashMap<>();
        int size = cacheTransactions.size();
        for (int i = size - 1; i >= 0; i--) {
            Transaction tx = cacheTransactions.get(i);
            List<TransactionInput> inputs = tx.getInputs();
            if (CollectionUtils.isEmpty(inputs)) {
                continue;
            }
            for (TransactionInput input : inputs) {
                String preUTXOKey = input.getPreUTXOKey();
                if (spentUTXOMap.containsKey(preUTXOKey)) {
                    txCacheManager.remove(tx.getHash());
                    cacheTransactions.remove(i);
                    LOGGER.warn("there has two or one tx try to spent same uxto={}," +
                            "old spend tx={}, other spend tx={}", preUTXOKey, spentUTXOMap.get(preUTXOKey), tx.getHash());
                    break;
                }
                if (!utxoMap.containsKey(preUTXOKey)) {
                    txCacheManager.remove(tx.getHash());
                    cacheTransactions.remove(i);
                    LOGGER.warn("utxo data map has no this uxto={}_tx={}", preUTXOKey, tx.getHash());
                    break;
                }
                spentUTXOMap.put(preUTXOKey, tx.getHash());
            }
        }

    }

    public Block packageNewBlock() {
        return packageNewBlock(peerKeyPair);
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

    /**
     * get the max height on the main/best chain
     *
     * @return
     */
    public long getBestMaxHeight() {
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
    synchronized public boolean persistBlockAndIndex(Block block, String sourceId, short version) {
        long height = block.getHeight();
        String blockHash = block.getHash();
        if (isExistInDB(block.getHash())) {
            return false;
        }
        //todo yuguojia valid the same miner mined second block
        if (hasFullCountBlocks(height)) {
            throw new RuntimeException("there has full count blocks on height=" + height + "_block=" + block);
        }

        //handle orphan block
        if (!preIsExitInDB(block) && !block.isgenesisBlock()) {
            BlockIndex preBlockIndex = getBlockIndexByHeight(height - 1);
            LOGGER.warn("Cannot get pre best block, put to cache and req pre blocks height={}_block={}_preBlock={}_preIndex={}"
                    , height, blockHash, block.getPrevBlockHash(), preBlockIndex);
            BlockFullInfo blockFullInfo = new BlockFullInfo(version, sourceId, block);
            blockCacheManager.putAndRequestPreBlocks(blockFullInfo);
            return false;
        }

        //valid
        if (!validBlock(block)) {
            LOGGER.error("Error block info, height={}_block={}", height, blockHash);
            return false;
        }

        String bestBlockHash = null;
        Block highestScoreBlock = null;
        BlockFullInfo blockFullInfo = new BlockFullInfo(version, sourceId, block);

        Block oldBestBlock = getBestBlockByHeight(height);
        String oldBestBlockHash = oldBestBlock == null ? null : oldBestBlock.getHash();
        LOGGER.info("got old best block on height={}_block={}, persist new block={}",
                height, oldBestBlockHash, block.getHash());

        if (block.isPreMiningBlock()) {
            bestBlockHash = block.getHash();
            highestScoreBlock = block;
        } else {
            List<Block> sameHeightBlocks = getBlocksByHeight(height);
            sameHeightBlocks.add(block);
            highestScoreBlock = getHighestScoreBlock(sameHeightBlocks);
            if (highestScoreBlock != null) {
                bestBlockHash = highestScoreBlock.getHash();
            }
        }
        LOGGER.info("got highest score block: height={}_block={}", height, bestBlockHash);

        blockMap.put(block.getHash(), block);
        blockCacheManager.remove(block.getHash());
        LOGGER.info("persisted block: height={}_block={}", height, block.getHash());

        persistIndex(block, bestBlockHash);

        Block newBestBlock = getBestBlockByHeight(height);
        if (highestScoreBlock != null && !StringUtils.equals(highestScoreBlock.getHash(), newBestBlock.getHash())) {
            throw new RuntimeException("persist error best block:" + highestScoreBlock + newBestBlock);
        }

        if (!block.isPreMiningBlock()) {
            broadBlockPersistedEvent(block, bestBlockHash);
        }
        if (oldBestBlock == null && highestScoreBlock != null) {
            nodeManager.parseDpos(highestScoreBlock);
            MinerScoreStrategy.refreshMinersScore(highestScoreBlock);
            persistPreOrphanBlock(blockFullInfo);
            LOGGER.error("The last best block is height={}_block={}", height, highestScoreBlock.getHash());
        }
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
            blockIndex = blockIndexMap.get(block.getHeight());
            boolean hasOldBest = blockIndex == null ? false : blockIndex.hasBestBlock();
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

        if (!needBuildUTXO) {
            return;
        }
        Block bestBlock = getBlock(bestBlockHash);
        if (null == bestBlock) {
            throw new RuntimeException("the block hash is " + bestBlockHash + " ,which don't has block");
        }
        //todo kongyu add the function 2018-3-28 16:47
        buildTXIndex(bestBlock);

        buildPubKeyMapIndex(bestBlock);

    }

    /**
     * build transaction index and utxo
     *
     * @param bestBlock
     */
    private void buildTXIndex(Block bestBlock) {
        if (null == bestBlock) {
            return;
        }
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
                            throw new RuntimeException("persist block error, no spent tx,tx=" + spentTxHash);
                        }
                        txIndex.addSpend(spentTxOutIndex, tx.getHash());
                        transactionIndexMap.put(txIndex.getTxHash(), txIndex);
                        //remove spent utxo
                        String utxoKey = UTXO.buildKey(spentTxHash, spentTxOutIndex);
                        if (utxoMap.get(utxoKey) == null) {
                            throw new RuntimeException("persist block error, no utxo, key={}" + utxoKey);
                        }
                        utxoMap.remove(utxoKey);
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

    private void buildPubKeyMapIndex(Block bestBlock) {
        if (null == bestBlock) {
            return;
        }
        if (!bestBlock.isEmptyTransactions()) {
            List<Transaction> transactionList = bestBlock.getTransactions();
            for (int txCount = 0; txCount < transactionList.size(); txCount++) {
                Transaction tx = transactionList.get(txCount);
                //todo kongyu add pubKeyMap 2018-03-26 11:00
                buildPubKeyMapWithTx(tx, txCount, bestBlock.getHash());
            }
        }
    }

    private void buildPubKeyMapWithTx(Transaction tx, int index, String blockHash) {
        if (null == tx || null == blockHash) {
            return;
        }
        if (index < 0) {
            return;
        }

        try {
            //inputs
            List<TransactionInput> inputs = tx.getInputs();
            if (!CollectionUtils.isEmpty(inputs)) {
                for (TransactionInput input : inputs) {
                    UnLockScript unLockScript = input.getUnLockScript();
                    if (null == unLockScript || null == unLockScript.getPkList()) {
                        continue;
                    }

                    List<String> pkList = unLockScript.getPkList();
                    for (String key : pkList) {
                        if (null == key) {
                            continue;
                        }
                        StringBuilder addrAndTxHash = new StringBuilder();
                        addrAndTxHash.append(ECKey.pubKey2Base58Address(key))
                                .append(":")
                                .append(tx.getHash())
                                .append(":")
                                .append(TransactionType.NORMAL.getCode());
                        byte[] addkey = addrAndTxHash.toString().getBytes();
                        byte[] value = blockHash.getBytes();
                        pubKeyMap.put(addkey, value);
                    }
                }
            }

            //outputs
            List<TransactionOutput> outputs = tx.getOutputs();
            if (!CollectionUtils.isEmpty(outputs)) {
                for (TransactionOutput output : outputs) {
                    if (null == output || null == output.getLockScript()) {
                        continue;
                    }

                    LockScript lockScript = output.getLockScript();
                    if (null == lockScript.getAddress()) {
                        continue;
                    }

                    StringBuilder addrAndTxHash = new StringBuilder();
                    addrAndTxHash.append(lockScript.getAddress())
                            .append(":")
                            .append(tx.getHash())
                            .append(":");
                    if (0 == index) {
                        addrAndTxHash.append(TransactionType.COINBASE.getCode());
                    } else {
                        addrAndTxHash.append(TransactionType.NORMAL.getCode());
                    }

                    byte[] addkey = addrAndTxHash.toString().getBytes();
                    byte[] value = blockHash.getBytes();
                    pubKeyMap.put(addkey, value);
                }
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private void broadBlockPersistedEvent(Block block, String bestBlockHash) {
        BlockPersistedEvent blockPersistedEvent = new BlockPersistedEvent();
        blockPersistedEvent.setHeight(block.getHeight());
        blockPersistedEvent.setBestBlockHash(block.getHash());
        blockPersistedEvent.setBestBlockHash(bestBlockHash);
        messageCenter.send(blockPersistedEvent);
    }

    public void broadCastBlock(Block block) {
        messageCenter.broadcast(block);
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
        clearAllIndexData();
        buildBlockIndexMap();
        //todo kongyu 2018-04-08 15:23 add
        if (!checkBlockNumbers()) {
            throw new RuntimeException("blockMap size is not equal blockIndexMap count number");
        }

        long maxHeight = blockIndexMap.size();
        for (long height = 1; height <= maxHeight; height++) {
            Block bestBlock = blockMap.get(blockIndexMap.get(height).getBestBlockHash());

            buildTXIndex(bestBlock);
            buildPubKeyMapIndex(bestBlock);
            MinerScoreStrategy.refreshMinersScore(bestBlock, true);
        }

        systemStatusManager.setSysStep(SystemStepEnum.LOADED_ALL_DATA);

        //todo kongyu&yuguojia verify the validity and completeness between the blocks info and indexes info
    }

    private boolean checkBlockNumbers() {
        long blockIndexSize = 0L;
        long blockMapSize = 0L;

        if (null == blockMap || null == blockIndexMap) {
            LOGGER.error("blockMap or blockIndexMap is null");
            return false;
        }

        blockMapSize = blockMap.size();
        if (blockMapSize < 0L || blockMapSize > Long.MAX_VALUE) {
            LOGGER.error("blockMapSize is error blockMapSize = {}", blockMapSize);
            return false;
        }

        List<Long> heights = Lists.newArrayList(blockIndexMap.keySet());
        if (CollectionUtils.isNotEmpty(heights)) {
            for (Long height : heights) {
                blockIndexSize += blockIndexMap.get(height).getBlockHashs().size();
            }
        }

        if (blockIndexSize < 0L || blockIndexSize > Long.MAX_VALUE) {
            LOGGER.error("blockIndexSize is error");
            return false;
        }

        LOGGER.info("blockIndexSize is {}, blockMapSize is {}", blockIndexSize, blockMapSize);
        return blockIndexSize == blockMapSize ? true : false;
    }

    private void buildBlockIndexMap() {
        Set<String> keySet = blockMap.keySet();
        for (String key : keySet) {
            Block block = blockMap.get(key);
            long height = block.getHeight();
            BlockIndex blockIndex = blockIndexMap.get(height);
            ArrayList<String> blockHashs = null;
            if (null == blockIndex) {
                blockHashs = Lists.newArrayList();
                blockHashs.add(block.getHash());
                blockIndex = new BlockIndex(height, blockHashs, -1);
            } else {
                blockIndex.getBlockHashs().add(block.getHash());
            }
            blockIndexMap.put(height, blockIndex);
        }

        buildBestBlockIndex();
    }

    private void buildBestBlockIndex() {
        long maxHeight = blockIndexMap.size();
        for (long height = 1; height <= maxHeight; height++) {
            BlockIndex blockIndex = blockIndexMap.get(height);
            if (null == blockIndex) {
                throw new RuntimeException("The height is " + height + " blockIndex is null");
            }

            ArrayList<String> blockHashs = blockIndex.getBlockHashs();
            if (CollectionUtils.isEmpty(blockHashs)) {
                continue;
            }

            List<Block> blocks = Lists.newArrayList();
            for (String hash : blockHashs) {
                Block block = blockMap.get(hash);
                if (null == block) {
                    continue;
                }
                blocks.add(block);
            }
            if (CollectionUtils.isEmpty(blocks)) {
                continue;
            }

            Block bestBlock = null;
            if (height <= Application.PRE_BLOCK_COUNT) {
                if (1 < blocks.size()) {
                    throw new RuntimeException("pre mining height is " + height + " has more one block");
                }
                bestBlock = blocks.get(0);
            } else {
                bestBlock = getMaxScoreBlock(blocks);
                if (null == bestBlock) {
                    LOGGER.info("The height is " + height + " ,which don't has best block");
                    continue;
                }
            }

            //todo kongyu update blockIndexMap best 2018-3-28 17:13
            updateBlockIndexMapBest(height, bestBlock.getHash());
        }
    }

    private void updateBlockIndexMapBest(long height, String blockHash) {
        boolean isUpdate = false;
        if (0 >= height && height > Long.MAX_VALUE) {
            LOGGER.error("block height is error");
            return;
        }
        if (null == blockHash) {
            LOGGER.error("blockHash is null");
            return;
        }

        BlockIndex blockIndex = blockIndexMap.get(height);
        if (null == blockIndex) {
            LOGGER.error("blockIndex is null");
            return;
        }

        ArrayList<String> blockHashs = blockIndex.getBlockHashs();
        if (CollectionUtils.isEmpty(blockHashs)) {
            LOGGER.error("blockHashs is empty");
            return;
        }

        for (int index = 0; index < blockHashs.size(); index++) {
            if (StringUtils.equals(blockHashs.get(index), blockHash)) {
                blockIndex.setBestIndex(index);
                isUpdate = true;
                break;
            }
        }

        if (!isUpdate) {
            LOGGER.error("blockIndexMap don't have best chain");
            return;
        }

        blockIndexMap.put(height, blockIndex);
    }

    public void clearAllIndexData() {
        blockIndexMap.clear();
        transactionIndexMap.clear();
        utxoMap.clear();
        myUTXOData.clear();
        //todo kongyu add 2018-3-28 11:49
        pubKeyMap.clear();
        //todo kongyu test 2018-3-28 19:19
    }

    public void removeBlocks(long height) {
        Set<Map.Entry<String, Block>> entries = blockMap.entrySet();
        for (Map.Entry<String, Block> entry : entries) {
            Block block = entry.getValue();
            if (height < block.getHeight()) {
                blockMap.remove(block.getHash());
            }
        }
    }

    public void clearAllDBData() {
        blockMap.clear();
        blockIndexMap.clear();
        transactionIndexMap.clear();
        utxoMap.clear();
        myUTXOData.clear();
    }

    public boolean hasFullCountBlocks(long height) {
        BlockIndex blockIndex = getBlockIndexByHeight(height);
        if (blockIndex != null &&
                blockIndex.getBlockHashCount() == nodeManager.getFullBlockCountByHeight(height)) {
            return true;
        }
        return false;
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


    public boolean hasBestPre(long preHeight, String preBlockHash) {
        BlockIndex preBlockIndex = blockIndexMap.get(preHeight);
        if (preBlockIndex != null) {
            if (StringUtils.equals(preBlockIndex.getBestBlockHash(), preBlockHash)) {
                return true;
            }
        }
        return false;
    }

    public int calcSignaturesScore(Block block) {
        int sigScore = 0;
        List<BlockWitness> otherPKSigs = block.getOtherWitnessSigPKS();
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
        // 排序 for has same highest max score
        blockList.sort(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                return o1.getHash().compareTo(o2.getHash());
            }
        });
        long height = blockList.get(0).getHeight();
        LOGGER.info("there has {} count blocks of height={}", blockList.size(), height);
        if (blockList.size() >= nodeManager.getFullBlockCountByHeight(height)) {
            LOGGER.info("force select max score block,heigh={}", height);
            return getMaxScoreBlock(blockList);
        }

        boolean canBigger = canMineBiggerScoreBlock(blockList);
        if (!canBigger) {
            return getMaxScoreBlock(blockList);
        }
        return null;
    }

    public Block getMaxScoreBlock(List<Block> blockList) {
        if (CollectionUtils.isEmpty(blockList)) {
            return null;
        }
        Block highestScoreBlock = null;
        long height = blockList.get(0).getHeight();
        int signedMaxScore = Integer.MIN_VALUE;
        for (Block block : blockList) {
            if (height != block.getHeight()) {
                throw new RuntimeException("there is different block height");
            }
            int score = calcSignaturesScore(block);
            //todo yuguojia has same highest score blocks
            if (score > signedMaxScore) {
                signedMaxScore = score;
                highestScoreBlock = block;
            }
        }
        LOGGER.info("getMaxScoreBlock height={}, max score={},block={}", height, signedMaxScore, highestScoreBlock.getHash());

        return highestScoreBlock;
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
            List<Integer> oneBlockGapSet = new LinkedList<>();
            for (String witnessBlockHash : witnessBlockHashList) {
                Block witnessBlock = getBlock(witnessBlockHash);
                if (witnessBlock != null) {
                    int gap = (int) (height - witnessBlock.getHeight());
                    signedGapSet.add(gap);
                    oneBlockGapSet.add(gap);
                }
            }
            LOGGER.info("the height={}_block={}_score={} signed high gaps is {}", height, block.getHash(), score, oneBlockGapSet);
        }
        LOGGER.info("the height={} all signed high gaps is {}, and max score is {}", height, signedGapSet, signedMaxScore);

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
        int so = 0;
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

    // check if the size of the block is appropriate.
    private boolean verifySize(Block block) {
        return block.sizeAllowed();
    }

    // check if the number of transactions in the block is appropriate.
    private boolean verifyTransactionNumber(Block block) {
        return block.getTransactions().size() >= MINIMUM_TRANSACTION_IN_BLOCK;
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

    public boolean validBlock(Block block) {
        long height = block.getHeight();
        String blockHash = block.getHash();
        if (!validBasic(block)) {
            LOGGER.error("Error block basic info, height={}_block={}", height, blockHash);
            return false;
        }

        if (!validBlockTransactions(block)) {
            LOGGER.error("Error block transactions, height={}_block={}", height, blockHash);
            return false;
        }
        return true;
    }

    public boolean validBlockTransactions(Block block) {
        LOGGER.info("begin to check the transactions of block {}", block.getHeight());

        if (block.isgenesisBlock()) {
            return true;
        }

        List<Transaction> transactions = block.getTransactions();
        if (CollectionUtils.isEmpty(transactions)) {
            LOGGER.error("transactions is empty");
            return false;
        }

        // check transactions
        int size = transactions.size();
        if (1 > size) {
            LOGGER.error("transactions is less than one");
            return false;
        }

        Transaction coinbaseTx = transactions.get(0);
        if (null == coinbaseTx) {
            LOGGER.error("Coinbase transaction is null");
            return false;
        }

        if (!validTxInputsIsNull(coinbaseTx)
                || !transactionService.validCoinBaseTx(coinbaseTx, block)) {
            LOGGER.error("Invalidate Coinbase transaction");
            return false;
        }

        HashSet<String> prevOutKey = new HashSet<>();
        for (int index = 1; index < size; index++) {
            if (!transactionService.verifyTransaction(transactions.get(index), prevOutKey, block)) {
                LOGGER.error("Invalidate transaction");
                return false;
            }
        }
        LOGGER.info("check the transactions success of block {}", block.getHeight());
        return true;
    }

    public boolean validTxInputsIsNull(Transaction tx) {
        if (null == tx) {
            return false;
        }

        if (null != tx.getInputs()) {
            return false;
        }
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
        if (!verifySize(block)) {
            LOGGER.error("Size of the block is illegal.");
            return false;
        }
        if (!block.isgenesisBlock() && !verifyTransactionNumber(block)) {
            LOGGER.error("Number of transaction in the block is illegal.");
            return false;
        }
        if (isExistInDB(blockHash)) {
            LOGGER.error("the block is exist in db");
            return false;
        }
        // todo yuguojia valid the block whether its pre block is best/main block(if best block exist)

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
        //todo yuguojia valid whether the miner could mining the height block

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

        List<BlockWitness> otherPKSigs = block.getOtherWitnessSigPKS();
        if (Application.PRE_BLOCK_COUNT >= block.getHeight()) {
            return true;
        }

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
            //todo yuguojia there are duplicate pks
            return false;
        }
        return true;
    }

    public void persistPreOrphanBlock(BlockFullInfo blockFullInfo) {
        Block block = blockFullInfo.getBlock();
        long height = block.getHeight();
        String blockHash = block.getHash();
        List<BlockFullInfo> nextConnectionBlocks = blockCacheManager.getNextConnectionBlocks(block.getHash());
        if (CollectionUtils.isNotEmpty(nextConnectionBlocks)) {
            for (BlockFullInfo nextBlockFullInfo : nextConnectionBlocks) {
                Block nextBlock = nextBlockFullInfo.getBlock();
                long nextHeight = nextBlock.getHeight();
                String nextBlockHash = nextBlock.getHash();
                String nextSourceId = nextBlockFullInfo.getSourceId();
                short nextVersion = nextBlockFullInfo.getVersion();
                LOGGER.info("persisted height={}_block={}, find orphan next block height={}_block={} to persist",
                        height, blockHash, nextHeight, nextBlockHash);
                if (!validBlock(nextBlock)) {
                    LOGGER.error("Error next block height={}_block={}", nextHeight, nextBlockHash);
                    blockCacheManager.remove(nextBlock.getHash());
                    continue;
                }
                persistBlockAndIndex(nextBlock, nextSourceId, nextVersion);
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
        if (!verifyMinerPermission(data)) {
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

    private boolean verifyMinerPermission(Block block) {
        BlockWitness blockWitness = block.getMinerFirstPKSig();
        if (!nodeManager.checkProducer(block)) {
            LOGGER.error("the height {}, this block miner is {}, the miners is {}"
                    , block.getHeight()
                    , block.getMinerSelfSigPKs().get(0).getAddress()
                    , nodeManager.getDposGroup(block.getHeight()));
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
