package com.higgsblock.global.chain.app.blockchain;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.Application;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.SystemStepEnum;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.consensus.*;
import com.higgsblock.global.chain.app.consensus.sign.service.CollectSignService;
import com.higgsblock.global.chain.app.consensus.syncblock.SyncBlockService;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.app.script.UnLockScript;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.Peer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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
     * Minimal witness number
     */
    private final static int MIN_WITNESS = 7;

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
    @Resource(name = "minerScoreMaps")
    private ConcurrentMap<String, Map> minerScoreMaps;
    @Autowired
    private ConcurrentMap<String, Peer> peerMap;
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
    private EventBus eventBus;
    @Autowired
    private SystemStatusManager systemStatusManager;

    @Autowired
    private AppConfig config;

    private Cache<String, Block> blockCache = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE).build();

    public final static List<String> WITNESS_ADDRESS_LIST = new ArrayList<>();

    static {
        String address1 = ECKey.fromPrivateKey("bae51e6f536e37a61b394dc1fe1b0ef66ae6631dd74d7f40ee070f709220736a").toBase58Address();
        String address2 = ECKey.fromPrivateKey("926bc1f045808cfc80a8307f463d797e98a57386a6803380d836541650c02ed6").toBase58Address();
        String address3 = ECKey.fromPrivateKey("bffeb7b53e66149e5bdfb6d104ac7a474aa463d0f74e6d5ca9dc95ba5aa50a44").toBase58Address();
        String address4 = ECKey.fromPrivateKey("b6478eb668404e15eb9e6dd82f3791ab689e714b545be30121634c040cddb7ac").toBase58Address();
        String address5 = ECKey.fromPrivateKey("a09baaa1f1cfed08dea7f0de97789469feacc416a4359b5cd03304e417e465eb").toBase58Address();

        String address6 = ECKey.fromPrivateKey("1954b19a2f78e1a1b5a42bdc042e66a671152cc7a3ccab40b1bca14685a6d962").toBase58Address();
        String address7 = ECKey.fromPrivateKey("4b8fdc264bb6907267ec680a48362266a35b8fd8ea81a467a4cd8297b1c45a48").toBase58Address();
        String address8 = ECKey.fromPrivateKey("93c6c5db174cb149c8838ee4a25b39da967cbcd0a16f15e6018e628e46bae3b7").toBase58Address();
        String address9 = ECKey.fromPrivateKey("c72b396b29e62d1f703f8518aa35613d34e27031d69d486d30f9d9b84db6ac03").toBase58Address();
        String address10 = ECKey.fromPrivateKey("a2fc438939010929b8a0bc71ef432a22157eaa1d452eff3aab5d28ff29f42580").toBase58Address();

        String address11 = ECKey.fromPrivateKey("b04f6e4a6f060051a3ba8d51f58698bc7397e80ac55b42852a2f56ad456d514c").toBase58Address();

        WITNESS_ADDRESS_LIST.add(address1);
        WITNESS_ADDRESS_LIST.add(address2);
        WITNESS_ADDRESS_LIST.add(address3);
        WITNESS_ADDRESS_LIST.add(address4);
        WITNESS_ADDRESS_LIST.add(address5);
        WITNESS_ADDRESS_LIST.add(address6);
        WITNESS_ADDRESS_LIST.add(address7);
        WITNESS_ADDRESS_LIST.add(address8);
        WITNESS_ADDRESS_LIST.add(address9);
        WITNESS_ADDRESS_LIST.add(address10);
        WITNESS_ADDRESS_LIST.add(address11);

        //WITNESS_ADDRESS_LIST.add("126n18bMNVGsMjoHvQ7oYcUwXKfzKadJWH");
    }

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
     * Persist the block and index data. If it is orphan block, add it to cache and do not persist to db
     * <p>
     * Used by:
     * 1.mining genesis block
     * 2.pre-mining block
     * 3.receiving block
     * </p>
     * <p>
     * <p>
     * Steps:
     * 1.Roughly check the witness signatures' count;
     * 2.Check if the block is an orphan block;
     * 3.Thoroughly validate the block;
     * 4.Save the block and block index;
     * 5.Broadcast the persist event;
     * 6.Update the block producer's score;
     * 7.Parse dpos;
     * 8.Chaining the orphan block to the chain;
     * </P>
     */
    public synchronized boolean persistBlockAndIndex(Block block, String sourceId, short version) {
        long height = block.getHeight();
        String blockHash = block.getHash();
        int sigCount = block.getWitnessSigCount();

        //Check the signature count roughly
        if (!block.isPreMiningBlock() && sigCount < MIN_WITNESS) {
            LOGGER.warn("The witness number is not enough : sigCount=>{}", sigCount);
            return false;
        }

        //Check if orphan block
        if (checkOrphanBlock(block, sourceId, version)) {
            LOGGER.warn("The block is an orphan block: height=>{} hash=>{}", height, blockHash);
            return false; //If the block was an orphan block always return false.
        }

        //Valid block thoroughly
        if (!validBlock(block)) {
            LOGGER.error("Validate block failed, height=>{} hash=>{}", height, blockHash);
            return false;
        }

        //Save block and index
        if (!saveBlockAndIndex(block, blockHash)) {
            LOGGER.error("Save block and block index failed, height=>{} hash=>{}", height, blockHash);
            return false;
        }

        //Broadcast persisted event
        if (!block.isPreMiningBlock()) {
            broadBlockPersistedEvent(block, blockHash);
        }

        //Do finishing job for the block
        finishingJobForBlock(block, sourceId, version);

        return true;
    }

    private boolean checkOrphanBlock(Block block, String sourceId, short version) {
        long height = block.getHeight();
        boolean isGenesisBlock = block.isgenesisBlock();

        if (!preIsExistInDB(block) && !isGenesisBlock) {
            BlockFullInfo blockFullInfo = new BlockFullInfo(version, sourceId, block);
            blockCacheManager.putAndRequestPreBlocks(blockFullInfo);

            LOGGER.warn("Cannot get pre best block, height={} hash={}", height, block.getHash());
            return true;
        }

        return false;
    }

    private boolean saveBlockAndIndex(Block block, String blockHash) {
        long height = block.getHeight();

        if (!block.isPreMiningBlock()) {
            List<Block> sameHeightBlocks = getBlocksByHeight(height);
            if (!CollectionUtils.isEmpty(sameHeightBlocks)) {
                throw new IllegalStateException("Same height block exists in DB");
            }
        }

        blockMap.put(blockHash, block);
        blockCacheManager.remove(blockHash);

        persistIndex(block, blockHash);

        return true;
    }

    private void finishingJobForBlock(Block block, String sourceId, short version) {
        MinerScoreStrategy.refreshMinersScore(block);

        nodeManager.parseDpos(block);

        chainingOrphanBlock(block, sourceId, version);
    }

    private void chainingOrphanBlock(Block block, String sourceId, short version) {
        BlockFullInfo blockFullInfo = new BlockFullInfo(version, sourceId, block);
        persistPreOrphanBlock(blockFullInfo);
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
                        myUTXOData.remove(utxoKey);
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
        blockPersistedEvent.setBlockHash(block.getHash());
        blockPersistedEvent.setBestBlockHash(bestBlockHash);
        eventBus.post(blockPersistedEvent);
        LOGGER.info("sent broad BlockPersistedEvent,height={}_block={}", block.getHeight(), block.getHash());
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

    //todo yuguojia just calculate some values now. handle md5 and snapshot file later.
    public void loadAllBlockData() {
        //todo kongyu 2018-5-4 11:25 从utxoMap中构建一个MyUtxoMap，专门为每个节点构建存放自己的utxomap
        if (!checkBlockNumbers()) {
            throw new RuntimeException("blockMap size is not equal blockIndexMap count number");
        }

        int dposScoreMapsize = scoreManager.getDposMinerSoreMap().size();
        if (dposScoreMapsize == 0) {
            long maxHeight = blockIndexMap.size();
            for (long height = 1; height <= maxHeight; height++) {
                Block bestBlock = blockMap.get(blockIndexMap.get(height).getBestBlockHash());
                MinerScoreStrategy.refreshMinersScore(bestBlock, true);
            }
        }

        if (!validateGenesisBlock()) {
            throw new RuntimeException("genesis block is incorrect");
        }
        //构建myUTXOMaps
        loadMyUtxo();
        systemStatusManager.setSysStep(SystemStepEnum.LOADED_ALL_DATA);
    }

    public boolean loadMyUtxo() {
        if (!myUTXOData.isEmpty()) {
            return true;
        }
        String addr = ECKey.pubKey2Base58Address(peerKeyPair.getPubKey());
        List<UTXO> myUtxoLists = utxoMap.values().stream().filter(utxo -> StringUtils.equals(addr, utxo.getAddress())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(myUtxoLists)) {
            return true;
        }
        for (UTXO utxo : myUtxoLists) {
            myUTXOData.put(utxo.getKey(), utxo);
        }
        return true;
    }

    public void loadAllBlockDataBackUp() {
        clearAllIndexData();
        buildBlockIndexMap();
        //todo if there any error sync all/part blocks
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
            throw new RuntimeException("blockIndexSize is error");
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

            List<Block> blocks = Lists.newArrayList();
            for (String hash : blockHashs) {
                Block block = blockMap.get(hash);
                blocks.add(block);
            }
            Block bestBlock = null;
            if (height <= Application.PRE_BLOCK_COUNT) {
                if (1 < blocks.size()) {
                    throw new RuntimeException("pre mining height is " + height + " has more one block");
                }
                bestBlock = blocks.get(0);
            } else {
                bestBlock = getMaxScoreBlock(blocks);
                if (null == bestBlock && height < maxHeight) {
                    //todo yuguojia sync blocks from this height
                    throw new RuntimeException("there has no best block,height=" + height);
                }
            }

            updateBlockIndexMapBest(height, bestBlock.getHash());
        }
    }

    private void updateBlockIndexMapBest(long height, String blockHash) {
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
                blockIndexMap.put(height, blockIndex);
                break;
            }
        }
    }

    public void clearAllIndexData() {
        blockIndexMap.clear();
        transactionIndexMap.clear();
        utxoMap.clear();
        myUTXOData.clear();
        pubKeyMap.clear();
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
        Iterator<String> scoreIterator = minerScoreMaps.keySet().iterator();
        Iterator<String> peerIterator = peerMap.keySet().iterator();
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

    public boolean isExist(Block block) {
        if (blockMap.get(block.getHash()) != null ||
                blockCacheManager.isContains(block.getHash())) {
            return true;
        }
        return false;
    }

    public boolean preIsExist(Block block) {
        if (preIsExistInDB(block) || preIsExistInCache(block)) {
            return true;
        }
        return false;
    }

    public boolean preIsExistInDB(Block block) {
        if (blockMap.get(block.getPrevBlockHash()) != null) {
            return true;
        }
        return false;
    }

    public boolean preIsExistInCache(Block block) {
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
            LOGGER.error("transactions is empty, block_hash={}", block.getHash());
            return false;
        }

        // check transactions
        int size = transactions.size();
        if (1 > size) {
            LOGGER.error("transactions is less than one, block_hash={}", block.getHash());
            return false;
        }

        for (int index = 0; index < size; index++) {
            if (0 == index) {
                if (!validTransactions(true, transactions.get(index), block)) {
                    return false;
                }
                continue;
            }
            if (!validTransactions(false, transactions.get(index), block)) {
                return false;
            }
        }
        LOGGER.info("check the transactions success of block {}", block.getHeight());
        return true;
    }

    public boolean validTransactions(boolean isCoinBaseTx, Transaction transaction, Block block) {
        if (null == transaction) {
            LOGGER.error("transaction is null");
            return false;
        }
        if (isCoinBaseTx) {
            if (!validTxInputsIsNull(transaction)
                    || !transactionService.validCoinBaseTx(transaction, block)) {
                LOGGER.error("Invalidate Coinbase transaction");
                return false;
            }
            return true;
        }
        HashSet<String> prevOutKey = new HashSet<>();
        if (!transactionService.verifyTransaction(transaction, prevOutKey, block)) {
            LOGGER.error("Invalidate transaction");
            return false;
        }
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
        if (!validBlockCommon(block)) {
            LOGGER.error("Validate block common failed");
            return false;
        }
        // todo yuguojia valid the block whether its pre block is best/main block(if best block exist)

        if (block.isPreMiningBlock()) {
            LOGGER.info("Block is pre-mining block, height=>{}", block.getHeight());
            return true;
        }

        if (!verifyMinerPermission(block)) {
            LOGGER.error("Validate the authority of the producer failed");
            return false;
        }

        if (!validAllWitnessSignatures(block)) {
            LOGGER.error("Validate signatures from witness failed");
            return false;
        }

        return true;
    }

    public boolean validRecommendBlock(Block block) {
        if (block == null) {
            LOGGER.error("Validate validRecommendBlock failed,block is null");
            return false;
        }
        if (!validBlockCommon(block)) {
            LOGGER.error("Validate block common failed");
            return false;
        }

        if (!validFirstWitnessSig(block)) {
            LOGGER.error("Validate witness failed");
            return false;
        }

        return true;
    }

    public boolean validFirstWitnessSig(Block block) {
        List<BlockWitness> witnessList = block.getOtherWitnessSigPKS();
        if (CollectionUtils.isEmpty(witnessList)) {
            LOGGER.error("Witness signature number is empty");
            return false;
        }

        if (witnessList.size() != 1) {
            LOGGER.error("Witness signature number is not correct,blockHash {}, witnessList {}", block.getHash(), witnessList);
            return false;
        }

        final BlockWitness blockWitness = witnessList.get(0);
        if (!ECKey.verifySign(block.getHash(), blockWitness.getSignature(), blockWitness.getPubKey())) {
            LOGGER.error("Block hash not match signature from witness when validate witness");
            return false;
        }

        final String tempAddress = ECKey.pubKey2Base58Address(blockWitness.getPubKey());
        //Check if in the witness list
        if (!WITNESS_ADDRESS_LIST.contains(tempAddress)) {
            LOGGER.error("The witness is invalid when validate witness");
            return false;
        }

        return true;
    }

    public boolean validBlockCommon(Block block) {
        short version = block.getVersion();
        String blockHash = block.getHash();
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
            LOGGER.error("The block is exist in db");
            return false;
        }

        if (!validProducerSignature(block)) {
            LOGGER.error("Validate signatures from producer failed");
            return false;
        }

        return true;
    }

    private boolean validProducerSignature(Block block) {
        // check signature
        BlockWitness minerPKSig = block.getMinerFirstPKSig();

        if (minerPKSig == null || !minerPKSig.valid()) {
            LOGGER.error("Invalid parameters");
            return false;
        }
        if (!ECKey.verifySign(block.getHash(), minerPKSig.getSignature(), minerPKSig.getPubKey())) {
            LOGGER.error("Validate the signature of miner failed");
            return false;
        }

        return true;
    }

    private boolean validAllWitnessSignatures(Block block) {
        List<BlockWitness> otherPKSigs = block.getOtherWitnessSigPKS();

        Set<String> pkSet = new HashSet<>();
        String tempAddress;
        for (BlockWitness pair : otherPKSigs) {
            if (!pair.valid()) {
                LOGGER.error("Invalid signature from witness");
                return false;
            }

            if (!ECKey.verifySign(block.getHash(), pair.getSignature(), pair.getPubKey())) {
                LOGGER.error("Block hash not match signature from witness");
                return false;
            }

            //Check the witness has the authority to witness the block
//            Block witnessBlock = blockMap.get(pair.getBlockHash());
//            if (witnessBlock == null) {
//                LOGGER.error("Can not find the block");
//                return false;
//            }
//
//            if (!StringUtils.equals(witnessBlock.getMinerFirstPKSig().getPubKey(), pair.getPubKey())) {
//                LOGGER.error("Miner public key not match");
//                return false;
//            }

            tempAddress = ECKey.pubKey2Base58Address(pair.getPubKey());
            //Check if in the witness list
            if (!WITNESS_ADDRESS_LIST.contains(tempAddress)) {
                LOGGER.error("The witness is invalid");
                return false;
            }

            pkSet.add(pair.getPubKey());
        }

        int trimSize = pkSet.size();
        if (trimSize < MIN_WITNESS) {
            LOGGER.error("The witness number is not enough : trim size=>{}", trimSize);
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
        if (!validBlockCommon(data)) {
            return false;
        }

        if (data.getVersion() < 0 ||
                data.getHeight() < 1 ||
                StringUtils.isEmpty(data.getPrevBlockHash()) ||
                CollectionUtils.isEmpty(data.getTransactions())) {
            return false;
        }

        if (isExist(data)) {
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
        if (!preIsExistInDB(block)) {
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

    private boolean validateGenesisBlock() {
        Block block = blockMap.get(config.getGenesisBlockHash());
        return null != block && block.isgenesisBlock();
    }
}
