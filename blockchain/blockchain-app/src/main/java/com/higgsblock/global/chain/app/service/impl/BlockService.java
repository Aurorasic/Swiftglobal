package com.higgsblock.global.chain.app.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.transaction.SortResult;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionCacheManager;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.SystemStepEnum;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.dao.IBlockRepository;
import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.service.*;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Zhao xiaogang
 * @date 2018-05-21
 */
@Service
@Slf4j
public class BlockService implements IBlockService {
    /**
     * The minimum number of transactions in the block
     */
    public static final int MINIMUM_TRANSACTION_IN_BLOCK = 2;
    /**
     * The minimal witness number
     */
    public final static int MIN_WITNESS = 7;
    private static final int LRU_CACHE_SIZE = 5;
    /**
     * The starting height of the main chain
     */
    private static final int MAIN_CHAIN_START_HEIGHT = 2;

    @Autowired
    private AppConfig config;
    @Autowired
    private IBlockRepository blockRepository;
    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;
    @Autowired
    private IBlockIndexService blockIndexService;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private TransactionCacheManager txCacheManager;
    @Autowired
    private UTXOServiceProxy utxoServiceProxy;
    @Autowired
    private IScoreService scoreService;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private BlockFormatter blockFormatter;
    @Autowired
    private IDposService dposService;
    @Autowired
    private IWitnessService witnessService;
    @Autowired
    private KeyPair peerKeyPair;
    @Autowired
    private ITransactionIndexService transactionIndexService;
    @Autowired
    private ITransactionFeeService transactionFeeService;
    @Autowired
    private SystemStatusManager systemStatusManager;

    private Cache<String, Block> blockCache = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE).build();

    /**
     * Gets witness sing message.
     *
     * @param height      the height
     * @param blockHash   the block hash
     * @param voteVersion the vote version
     * @return the witness sing message
     */
    public static String getWitnessSingMessage(long height, String blockHash, int voteVersion) {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(function.hashLong(height))
                .append(function.hashString(null == blockHash ? Strings.EMPTY : blockHash, Charsets.UTF_8))
                .append(function.hashInt(voteVersion));
        return function.hashString(builder.toString(), Charsets.UTF_8).toString();
    }

    /**
     * Valid sign boolean.
     *
     * @param height      the height
     * @param blockHash   the block hash
     * @param voteVersion the vote version
     * @param sign        the sign
     * @param pubKey      the pub key
     * @return the boolean
     */
    public static boolean validSign(long height, String blockHash, int voteVersion, String sign, String pubKey) {
        String message = getWitnessSingMessage(height, blockHash, voteVersion);
        return ECKey.verifySign(message, sign, pubKey);
    }


    @Override
    public boolean isExistInDB(long height, String blockHash) {
        BlockIndex blockIndex = blockIndexService.getBlockIndexByHeight(height);
        return blockIndex != null && blockIndex.containsBlockHash(blockHash);

    }

    @Override
    public boolean isExist(Block block) {
        if (orphanBlockCacheManager.isContains(block.getHash())) {
            return true;
        }
        if (isExistInDB(block.getHeight(), block.getHash())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean preIsExistInDB(Block block) {
        if (block == null) {
            return false;
        }
        return blockRepository.findByBlockHash(block.getPrevBlockHash()) != null;
    }

    @Override
    public Block getBlockByHash(String blockHash) {
        BlockEntity blockEntity = blockRepository.findByBlockHash(blockHash);
        if (blockEntity != null) {
            return blockFormatter.parse(blockEntity.getData());
        }
        return null;

    }

    @Override
    public List<Block> getBlocksByHeight(long height) {
        BlockIndex blockIndex = blockIndexService.getBlockIndexByHeight(height);
        List<Block> blocks = new LinkedList<>();
        if (blockIndex != null) {
            ArrayList<String> blockHashes = blockIndex.getBlockHashs();
            blockHashes.forEach(blockHash -> {
                Block otherBlock = getBlockByHash(blockHash);
                if (otherBlock != null) {
                    blocks.add(otherBlock);
                }
            });
        }

        return blocks;
    }

    @Override
    public List<Block> getBlocksExcept(long height, String exceptBlockHash) {
        BlockIndex blockIndex = blockIndexService.getBlockIndexByHeight(height);
        if (blockIndex == null) {
            return null;
        }
        ArrayList<String> blockHashes = blockIndex.getBlockHashs();
        List<Block> blocks = new LinkedList<>();

        for (String blockHash : blockHashes) {
            if (StringUtils.equals(blockHash, exceptBlockHash)) {
                continue;
            }
            Block otherBlock = getBlockByHash(blockHash);
            if (otherBlock != null) {
                blocks.add(otherBlock);
            }
        }
        return blocks;
    }

    @Override
    public Block getBestBlockByHeight(long height) {
        BlockIndex blockIndex = blockIndexService.getBlockIndexByHeight(height);
        if (blockIndex == null) {
            return null;
        }

        String bestBlockHash = blockIndex.getBestBlockHash();
        if (StringUtils.isEmpty(bestBlockHash)) {
            return null;
        }
        return getBlockByHash(bestBlockHash);
    }

    public void saveBlock(Block block) throws Exception {

        BlockEntity blockEntity = new BlockEntity();
        blockEntity.setBlockHash(block.getHash());
        blockEntity.setHeight(block.getHeight());
        blockEntity.setData(blockFormatter.format(block));
        blockRepository.save(blockEntity);
        LOGGER.info("saved block:{}", block.getSimpleInfo());
    }

    @Override
    public boolean checkBlockNumbers() {
        //TODO: zhao xiaogang  should optimize  2018-05-22
        return true;
    }

    @Override
    public boolean isFirstBlockByHeight(Block block) {
        if (block.isGenesisBlock()) {
            return true;
        }
        return null == blockIndexService.getBlockIndexByHeight(block.getHeight());
    }

    @Override
    public Block getToBeBestBlock(Block block) {
        if (block.isGenesisBlock()) {
            return null;
        }
        if (block.getHeight() - DposService.CONFIRM_BEST_BLOCK_MIN_NUM < MAIN_CHAIN_START_HEIGHT) {
            return null;
        }
        Block bestBlock = recursePreBlock(block.getPrevBlockHash(), DposService.CONFIRM_BEST_BLOCK_MIN_NUM);
        if (bestBlock == null) {
            LOGGER.info("h-N block has be confirmed,current height:{}", block.getHeight());
            return null;
        }
        // h-N-1 block has ready be bestchain
        Block preBestBlock = getBlockByHash(bestBlock.getPrevBlockHash());
        Block bestBlockOfHeight = getBestBlockByHeight(preBestBlock.getHeight());
        if (preBestBlock == null || bestBlockOfHeight == null) {
            //todo huangshengli business error ,failure bypass 2018-06-30
            LOGGER.warn("Business Error,h-N-1 block not found,ToBeBestBlock:[{},{}],preBlockHash:{}", bestBlock.getHash(), bestBlock.getHeight(), bestBlock.getPrevBlockHash());
            return null;
        }
        if (!preBestBlock.getHash().equals(bestBlockOfHeight.getHash())) {
            LOGGER.warn("Business Error,h-N-1 blockhash:{} is not match that:{} of the height:{}", preBestBlock.getHash(), bestBlockOfHeight.getHash(), preBestBlock.getHeight());
            return null;
        }

        return bestBlock;
    }

    /**
     * get last best BlockIndex
     *
     * @return the last best blockIndex
     */
    @Override
    public BlockIndex getLastBestBlockIndex() {
        BlockIndex index = blockIndexService.getLastBlockIndex();
        if (index.hasBestBlock()) {
            return index;
        }

        long maxHeight = index.getHeight();
        while (maxHeight-- > 0) {
            BlockIndex preBlockIndex = blockIndexService.getBlockIndexByHeight(maxHeight);
            if (preBlockIndex.hasBestBlock()) {
                return preBlockIndex;
            }
        }

        return blockIndexService.getBlockIndexByHeight(1);
    }

    /**
     * Check witness signatures
     *
     * @param block the block
     * @return the boolean
     */
    @Override
    public boolean checkWitnessSignatures(Block block) {
        String blockLogInfo = block.getSimpleInfo();

        List<BlockWitness> witnessSigPKS = block.getOtherWitnessSigPKS();
        if (CollectionUtils.isEmpty(witnessSigPKS) || witnessSigPKS.size() < MIN_WITNESS) {
            int signatureSize = CollectionUtils.isEmpty(witnessSigPKS) ? 0 : witnessSigPKS.size();
            LOGGER.warn("The witness signatures is empty or the signature number is not enough,current size={},{}", signatureSize, blockLogInfo);
        }

        Set<String> pkSet = Sets.newHashSet();
        String tempAddress;
        for (BlockWitness pair : witnessSigPKS) {
            if (!pair.valid()) {
                LOGGER.warn("Invalid signature from witness,{}", blockLogInfo);
                return false;
            }

            String pubKey = pair.getPubKey();
            String signature = pair.getSignature();
            boolean validSign = validSign(block.getHeight(), block.getHash(), block.getVoteVersion(), signature, pubKey);
            if (!validSign) {
                LOGGER.warn("Block hash not match signature from witness,{}", blockLogInfo);
                return false;
            }

            tempAddress = ECKey.pubKey2Base58Address(pair.getPubKey());
            if (!witnessService.isWitness(tempAddress)) {
                LOGGER.warn("The witness is invalid,{}", blockLogInfo);
                return false;
            }

            pkSet.add(pair.getPubKey());
        }

        int trimSize = pkSet.size();
        if (trimSize < MIN_WITNESS) {
            LOGGER.warn("The witness's valid signature number is not enough : current size={}", trimSize);
            return false;
        }

        return true;
    }

    /**
     * Check the producer of the block.
     *
     * @param block
     * @return the boolean
     */
    @Override
    public boolean checkBlockProducer(Block block) {
        // 1.check the miner signature
        boolean result = checkProducerSignature(block);
        if (!result) {
            return false;
        }

        // 2.check the current rotation whether the miner should produce the block
        result = dposService.checkProducer(block);
        if (!result) {
            return false;
        }

        LOGGER.info("successfully validate block from producer");
        return true;
    }

    /**
     * packageNewBlock
     *
     * @param preBlockHash
     * @return
     */
    @Override
    public Block packageNewBlock(String preBlockHash) {
        Block block = packageNewBlockForPreBlockHash(preBlockHash, peerKeyPair);
        if (block == null) {
            LOGGER.error("cannot packageNewBlock on preBlockHash:{} ", preBlockHash);
        }
        return block;
    }

    public Block packageNewBlockForPreBlockHash(String preBlockHash, KeyPair keyPair) {
        BlockIndex lastBlockIndex = blockIndexService.getLastBlockIndex();
        if (lastBlockIndex == null) {
            throw new IllegalStateException("The best block index can not be null");
        }

        Collection<Transaction> cacheTmpTransactions = txCacheManager.getTransactionMap().asMap().values();
        ArrayList cacheTransactions = new ArrayList(cacheTmpTransactions);

        List txOfUnSpentUtxos = transactionIndexService.getTxOfUnSpentUtxo(preBlockHash, cacheTransactions);

        if (txOfUnSpentUtxos.size() < MINIMUM_TRANSACTION_IN_BLOCK - 1) {
            LOGGER.warn("There are no enough transactions, less than two, for packaging a block base on={}", preBlockHash);
            return null;
        }

        long nextBestBlockHeight = lastBlockIndex.getHeight() + 1;
        LOGGER.info("try to packageNewBlock, height={}", nextBestBlockHeight);
        List<Transaction> transactions = Lists.newLinkedList();

        //added by tangKun: order transaction by fee weight
        SortResult sortResult = transactionFeeService.orderTransaction(preBlockHash, txOfUnSpentUtxos);
        List<Transaction> canPackageTransactionsOfBlock = txOfUnSpentUtxos;
        Map<String, Money> feeTempMap = sortResult.getFeeMap();
        // if sort result overrun is true so do sub cache transaction
        if (sortResult.isOverrun()) {
            canPackageTransactionsOfBlock = transactionFeeService.getCanPackageTransactionsOfBlock(txOfUnSpentUtxos);
            feeTempMap = new HashMap<>(canPackageTransactionsOfBlock.size());
            for (Transaction tx : canPackageTransactionsOfBlock) {
                feeTempMap.put(tx.getHash(), sortResult.getFeeMap().get(tx.getHash()));
            }
        }

        if (lastBlockIndex.getHeight() >= 1) {
            Transaction coinBaseTx = transactionFeeService.buildCoinBaseTx(0L, (short) 1, feeTempMap, nextBestBlockHeight);
            transactions.add(coinBaseTx);
        }

        transactions.addAll(canPackageTransactionsOfBlock);

        Block block = new Block();
        block.setVersion((short) 1);
        block.setBlockTime(System.currentTimeMillis());
        block.setPrevBlockHash(preBlockHash);
        block.setTransactions(transactions);
        block.setHeight(nextBestBlockHeight);
        block.setPubKey(keyPair.getPubKey());

        //Before collecting signs from witnesses just cache the block firstly.
        String sig = ECKey.signMessage(block.getHash(), keyPair.getPriKey());
        block.initMinerPkSig(keyPair.getPubKey(), sig);
        blockCache.put(block.getHash(), block);
        LOGGER.info("new block was packed successfully, block height={}, hash={}", block.getHeight(), block.getHash());
        return block;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized boolean persistBlockAndIndex(Block block) {
        //Save block and index
        Block newBestBlock = saveBlockCompletely(block);

        //add unconfirmed utxos and remove confirmed height blocks in cache
        utxoServiceProxy.addNewBlock(newBestBlock, block);

        //refresh cache
        refreshCache(block.getHash(), block);

        //Broadcast persisted event
        broadBlockPersistedEvent(block, newBestBlock);

        return true;
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
     * </P>
     */
    public Block saveBlockCompletely(Block block) {
        try {
            //step 1
            saveBlock(block);

            boolean isFirst = isFirstBlockByHeight(block);
            Block newBestBlock = null;
            if (isFirst) {
                newBestBlock = getToBeBestBlock(block);
            } else {
                LOGGER.info("block:{} is not first at height :{}", block.getHash(), block.getHeight());
            }
            //step 2 whether this block can be confirmed pre N block
            blockIndexService.addBlockIndex(block, newBestBlock);

            if (block.isGenesisBlock()) {
                //step 3
                scoreService.refreshMinersScore(block);
                //step 4
                dposService.calcNextDposNodes(block, block.getHeight());
                return newBestBlock;
            }
            if (isFirst && newBestBlock != null) {
                LOGGER.info("to be confirmed best block:{}", newBestBlock.getSimpleInfo());
                scoreService.refreshMinersScore(newBestBlock);
                List<String> nextDposAddressList = dposService.calcNextDposNodes(newBestBlock, block.getHeight());
                scoreService.setSelectedDposScore(nextDposAddressList);
                //step5
                freshPeerMinerAddr(newBestBlock);
            }
            return newBestBlock;
        } catch (Exception e) {
            LOGGER.error(String.format("Save block and block index failed, height=%s_hash=%s", block.getHeight(), block.getHash()), e);
            throw new IllegalStateException("Save block completely failed");
        }
    }

    private boolean checkProducerSignature(Block block) {
        final BlockWitness minerPKSig = block.getMinerFirstPKSig();
        if (minerPKSig == null || !minerPKSig.valid()) {
            LOGGER.error("The miner signature is invalid:{}", block.getSimpleInfo());
            return false;
        }
        if (!ECKey.verifySign(block.getHash(), minerPKSig.getSignature(), minerPKSig.getPubKey())) {
            LOGGER.error("Validate the signature of miner failed:{}", block.getSimpleInfo());
            return false;
        }

        return true;
    }

    /**
     * refresh txCacheManager
     *
     * @param blockHash
     * @param block
     */
    private void refreshCache(String blockHash, Block block) {
        orphanBlockCacheManager.remove(blockHash);

        block.getTransactions().stream().forEach(tx -> {
            txCacheManager.remove(tx.getHash());
        });
        //remove by utxo key
        //todo yuguojia 2018-7-9 add new utxo when confirmed a best block on other blocks of the same height
        Map<String, Transaction> transactionMap = txCacheManager.getTransactionMap().asMap();
        List<String> spendUTXOKeys = block.getSpendUTXOKeys();
        for (Transaction tx : transactionMap.values()) {
            for (String spendUTXOKey : spendUTXOKeys) {
                if (tx.containsSpendUTXO(spendUTXOKey)) {
                    txCacheManager.remove(tx.getHash());
                    break;
                }
            }
        }
    }

    /**
     * fresh peer's minerAddress to connect ahead
     *
     * @param toBeBestBlock
     */
    private void freshPeerMinerAddr(Block toBeBestBlock) {
        List<String> dposGroupBySn = new LinkedList<>();
        long sn = dposService.getSn(toBeBestBlock.getHeight());
        List<String> dpos = dposService.getDposGroupBySn(sn);
        if (!CollectionUtils.isEmpty(dpos)) {
            dposGroupBySn.addAll(dpos);
        }
        dpos = dposService.getDposGroupBySn(sn + 1);
        if (!CollectionUtils.isEmpty(dpos)) {
            dposGroupBySn.addAll(dpos);
        }
        peerManager.setMinerAddresses(dposGroupBySn);
    }

    public void broadBlockPersistedEvent(Block block, Block newBestBlock) {
        BlockPersistedEvent blockPersistedEvent = new BlockPersistedEvent();
        blockPersistedEvent.setHeight(block.getHeight());
        blockPersistedEvent.setBlockHash(block.getHash());
        blockPersistedEvent.setConfirmedNewBestBlock(newBestBlock == null ? false : true);
        eventBus.post(blockPersistedEvent);
        LOGGER.info("sent broad BlockPersistedEvent,height={},block={}", block.getHeight(), block.getHash());
    }

    private Block recursePreBlock(String preBlockHash, int preHeightNum) {

        Block preBlock = getBlockByHash(preBlockHash);
        if (preBlock == null) {
            LOGGER.warn("preblock is null,may be db transaction error or sync error,blockhash:{}", preBlockHash);
            throw new IllegalStateException("can not find block,blockhash:" + preBlockHash);
        }
        if (preBlock.getHash().equals(blockIndexService.getBlockIndexByHeight(preBlock.getHeight()).getBestBlockHash())) {
            LOGGER.info("block[blockhash:{},height:{}]has be confirmed on best chain,skip this", preBlock.getHash(), preBlock.getHeight());
            return null;
        }
        if (preHeightNum-- > 1) {
            return recursePreBlock(preBlock.getPrevBlockHash(), preHeightNum);
        }
        LOGGER.info("found tobeBest block:{} height:{} ", preBlock.getHash(), preBlock.getHeight());
        return preBlock;
    }

    public void loadAllBlockData() {
        //todo kongyu 2018-7-17 loadAllBlockData backup
        /*
        1.先校验创世块
        2.然后再校验区块索引信息
        3.最后设置系统状态为LOADED_ALL_DATA
         */
        if (!checkBlockNumbers()) {
            throw new RuntimeException("blockMap size is not equal blockIndexMap count number");
        }

        if (!validateGenesisBlock()) {
            throw new RuntimeException("genesis block is incorrect");
        }

        systemStatusManager.setSysStep(SystemStepEnum.LOADED_ALL_DATA);
    }

    private boolean validateGenesisBlock() {
        Block block = getBlockByHash(config.getGenesisBlockHash());
        return null != block &&
                block.getHeight() == 1 &&
                block.getPrevBlockHash() == null;
    }
}
