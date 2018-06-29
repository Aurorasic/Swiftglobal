package com.higgsblock.global.chain.app.blockchain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.SystemStepEnum;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.service.impl.BlockDaoService;
import com.higgsblock.global.chain.app.service.impl.BlockIdxDaoService;
import com.higgsblock.global.chain.app.service.impl.DictionaryService;
import com.higgsblock.global.chain.app.service.impl.TransDaoService;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    /**
     * the max distance that miner got signatures
     */
    private static short MAX_DISTANCE_SIG = 50;

    /**
     * Minimal witness number
     */
    public final static int MIN_WITNESS = 7;


    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private BlockCacheManager blockCacheManager;

    @Autowired
    private KeyPair peerKeyPair;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private SystemStatusManager systemStatusManager;

    @Autowired
    private AppConfig config;

    @Autowired
    private BlockIdxDaoService blockIdxDaoService;

    @Autowired
    private BlockDaoService blockDaoService;

    @Autowired
    private TransDaoService transDaoService;

    @Autowired
    private DictionaryService dictionaryService;

    private Cache<String, Block> blockCache = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE).build();

    public final static List<String> WITNESS_ADDRESS_LIST = new ArrayList<>();

    public final static List<WitnessEntity> WITNESS_ENTITY_LIST = new ArrayList<>();

    @Autowired
    private TransactionFeeService transactionFeeService;

    public void initWitness() {
        List<String> witnessAddrList = config.getWitnessAddrList();
        List<Integer> witnessSocketPortList = config.getWitnessSocketPortList();
        List<Integer> witnessHttpPortList = config.getWitnessHttpPortList();
        List<String> witnessPubkeyList = config.getWitnessPubkeyList();

        int size = witnessAddrList.size();
        for (int i = 0; i < size; i++) {
            WitnessEntity entity = getEntity(
                    witnessAddrList.get(i),
                    witnessSocketPortList.get(i),
                    witnessHttpPortList.get(i),
                    witnessPubkeyList.get(i));

            WITNESS_ENTITY_LIST.add(entity);
        }

        WITNESS_ENTITY_LIST.stream().forEach(entity -> {
            WITNESS_ADDRESS_LIST.add(ECKey.pubKey2Base58Address(entity.getPubKey()));
        });
        LOGGER.info("the witness list is {}", WITNESS_ENTITY_LIST);
    }

    private static WitnessEntity getEntity(String ip, int socketPort, int httpPort, String pubKey) {
        WitnessEntity entity = new WitnessEntity();
        entity.setSocketPort(socketPort);
        entity.setIp(ip);
        entity.setPubKey(pubKey);
        entity.setHttpPort(httpPort);

        return entity;
    }

    public Block packageNewBlock(KeyPair keyPair) {
        LatestBestBlockIndex bestBlockIndex = dictionaryService.getLatestBestBlockIndex();
        if (bestBlockIndex == null) {
            throw new IllegalStateException("The best block index can not be null");
        }

        Collection<Transaction> cacheTmpTransactions = txCacheManager.getTransactionMap().asMap().values();
        ArrayList cacheTransactions = new ArrayList(cacheTmpTransactions);

        transDaoService.removeDoubleSpendTx(cacheTransactions);

        if (cacheTransactions.size() < MINIMUM_TRANSACTION_IN_BLOCK - 1) {
            LOGGER.warn("There are no enough transactions, less than two, for packaging a block.");
            return null;
        }

        long nextBestBlockHeight = bestBlockIndex.getHeight() + 1;
        LOGGER.info("try to packageNewBlock, height={}", nextBestBlockHeight);
        List<Transaction> transactions = Lists.newLinkedList();

        //added by tangKun: order transaction by fee weight
        SortResult sortResult = transactionFeeService.orderTransaction(cacheTransactions);
        List<Transaction> canPackageTransactionsOfBlock = cacheTransactions;
        Map<String, Money> feeTempMap = sortResult.getFeeMap();
        // if sort result overrun is true so do sub cache transaction
        if (sortResult.isOverrun()) {
            canPackageTransactionsOfBlock = transactionFeeService.getCanPackageTransactionsOfBlock(cacheTransactions);
            feeTempMap = new HashMap<>(canPackageTransactionsOfBlock.size());
            for (Transaction tx : canPackageTransactionsOfBlock) {
                feeTempMap.put(tx.getHash(), sortResult.getFeeMap().get(tx.getHash()));
            }
        }

        if (bestBlockIndex.getHeight() >= 1) {
            Transaction coinBaseTx = transactionFeeService.buildCoinBaseTx(0L, (short) 1, feeTempMap, nextBestBlockHeight);
            transactions.add(coinBaseTx);
        }

        transactions.addAll(canPackageTransactionsOfBlock);

        Block block = new Block();
        block.setVersion((short) 1);
        block.setBlockTime(System.currentTimeMillis());
        block.setPrevBlockHash(bestBlockIndex.getBestBlockHash());
        block.setTransactions(transactions);
        block.setHeight(nextBestBlockHeight);
        block.setPubKey(keyPair.getPubKey());


        //Before collecting signs from witnesses just cache the block firstly.
        String sig = ECKey.signMessage(block.getHash(), keyPair.getPriKey());
        block.initMinerPkSig(keyPair.getPubKey(), sig);
        blockCache.put(block.getHash(), block);

        return block;
    }

    public Block packageNewBlock() {
        return packageNewBlock(peerKeyPair);
    }

    /**
     * get the max height on the main/best chain
     */
    public long getBestMaxHeight() {
        LatestBestBlockIndex index = dictionaryService.getLatestBestBlockIndex();

        return index == null ? 0 : index.getHeight();
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
        if (!block.isGenesisBlock() && sigCount < MIN_WITNESS) {
            LOGGER.warn("The witness number is not enough : sigCount=>{}", sigCount);
            return false;
        }

        //Check if orphan block
        if (checkOrphanBlock(block, sourceId, version)) {
            LOGGER.warn("The block is an orphan block: height=>{} hash=>{}", height, blockHash);
            //If the block was an orphan block always return false.
            return false;
        }

        //Valid block thoroughly
        if (!validBlock(block)) {
            LOGGER.error("Validate block failed, height=>{} hash=>{}", height, blockHash);
            return false;
        }

        //Save block and index
        if (!saveBlockCompletely(block, blockHash)) {
            LOGGER.error("Save block and block index failed, height=>{} hash=>{}", height, blockHash);
            return false;
        }

        //Broadcast persisted event
        broadBlockPersistedEvent(block, blockHash);

        //Do last job for the block
        doLastJobForBlock(block, sourceId, version);

        return true;
    }

    public boolean checkOrphanBlock(Block block, String sourceId, short version) {
        long height = block.getHeight();
        boolean isGenesisBlock = block.isGenesisBlock();

        if (!isGenesisBlock && !preIsExistInDB(block)) {
            BlockFullInfo blockFullInfo = new BlockFullInfo(version, sourceId, block);
            blockCacheManager.putAndRequestPreBlocks(blockFullInfo);

            LOGGER.warn("Cannot get pre best block, height={} hash={}", height, block.getHash());
            return true;
        }

        return false;
    }

    private boolean saveBlockCompletely(Block block, String blockHash) {
        try {
            blockDaoService.saveBlockCompletely(block, blockHash);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Save block completely failed");
        }

        return true;
    }

    public void doLastJobForBlock(Block block, String sourceId, short version) {
        chainingOrphanBlock(block, sourceId, version);
    }

    private void chainingOrphanBlock(Block block, String sourceId, short version) {
        BlockFullInfo blockFullInfo = new BlockFullInfo(version, sourceId, block);
        persistPreOrphanBlock(blockFullInfo);
    }

    public void broadBlockPersistedEvent(Block block, String bestBlockHash) {
        BlockPersistedEvent blockPersistedEvent = new BlockPersistedEvent();
        blockPersistedEvent.setHeight(block.getHeight());
        blockPersistedEvent.setBlockHash(block.getHash());
        blockPersistedEvent.setBestBlockHash(bestBlockHash);
        eventBus.post(blockPersistedEvent);
        LOGGER.info("sent broad BlockPersistedEvent,height={}_block={}", block.getHeight(), block.getHash());
    }

    public void loadAllBlockData() {
        if (!checkBlockNumbers()) {
            throw new RuntimeException("blockMap size is not equal blockIndexMap count number");
        }

        if (!validateGenesisBlock()) {
            throw new RuntimeException("genesis block is incorrect");
        }

        systemStatusManager.setSysStep(SystemStepEnum.LOADED_ALL_DATA);
    }

    private boolean checkBlockNumbers() {
        return blockDaoService.checkBlockNumbers();
    }

    public Block getBestBlockByHeight(long height) {
        return blockDaoService.getBestBlockByHeight(height);
    }

    public List<Block> getBlocksByHeight(long height) {
        return blockDaoService.getBlocksByHeight(height);
    }

    public void printAllBlockData() {
        blockDaoService.printAllBlockData();
    }

    private boolean verifySize(Block block) {
        return block.sizeAllowed();
    }

    private boolean verifyTransactionNumber(Block block) {
        return block.getTransactions().size() >= MINIMUM_TRANSACTION_IN_BLOCK;
    }

    public boolean isExistInDB(long height, String blockHash) {
        return blockDaoService.isExistInDB(height, blockHash);
    }

    public boolean isExist(Block block) {
        return blockDaoService.isExist(block);
    }

    public boolean preIsExistInDB(Block block) {
        return blockDaoService.preIsExistInDB(block);
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

        if (block.isGenesisBlock()) {
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
            boolean isCoinBaseTx = index == 0 ? true : false;
            if (!validTransactions(isCoinBaseTx, transactions.get(index), block)) {
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
            if (!transaction.isEmptyInputs()
                    || !transactionService.validCoinBaseTx(transaction, block)) {
                LOGGER.error("Invalidate Coinbase transaction");
                return false;
            }
            return true;
        }

        if (!transactionService.verifyTransaction(transaction, block)) {
            LOGGER.error("Invalidate transaction");
            return false;
        }
        return true;
    }

    public boolean validBasic(Block block) {
        if (!validBlockCommon(block)) {
            LOGGER.error("Validate block common failed");
            return false;
        }
        // todo yuguojia 2018-6-4 valid the block whether its pre block is best/main block(if best block exist) for forking blocks

        if (block.isGenesisBlock()) {
            LOGGER.info("Block is genesis block, height=>{}", block.getHeight());
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

        if (!block.isGenesisBlock() && StringUtils.isEmpty(prevBlockHash)) {
            return false;
        }

        if (!verifySize(block)) {
            LOGGER.error("Size of the block is illegal.");
            return false;
        }

        if (!block.isGenesisBlock() && !verifyTransactionNumber(block)) {
            LOGGER.error("Number of transaction in the block is illegal.");
            return false;
        }

        if (isExistInDB(block.getHeight(), blockHash)) {
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
                    , nodeManager.getDposGroupByHeihgt(block.getHeight()));
            return false;
        }

        if (!ECKey.verifySign(block.getHash(),
                blockWitness.getSignature(),
                blockWitness.getPubKey())) {
            return false;
        }

        return true;
    }

    private boolean validateGenesisBlock() {
        Block block = blockDaoService.getBlockByHash(config.getGenesisBlockHash());
        return null != block && block.isGenesisBlock();
    }


}
