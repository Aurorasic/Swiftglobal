package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.consensus.MinerScoreStrategy;
import com.higgsblock.global.chain.app.blockchain.consensus.NodeProcessor;
import com.higgsblock.global.chain.app.blockchain.formatter.BlockFormatter;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionCacheManager;
import com.higgsblock.global.chain.app.dao.IBlockRepository;
import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Zhao xiaogang
 * @date 2018-05-21
 */
@Service
@Slf4j
public class BlockService implements IBlockService {

    @Autowired
    private IBlockRepository blockRepository;

    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Autowired
    private BlockIndexService blockIndexService;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private NodeProcessor nodeProcessor;

    @Autowired
    private MinerScoreStrategy minerScoreStrategy;

    @Autowired
    private PeerManager peerManager;

    @Autowired
    private BlockFormatter blockFormatter;


    private static final int MAIN_CHAIN_START_HEIGHT = 2;

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
        if (blockEntity == null) {
            LOGGER.error("not found the block by blockHash ={} ", blockHash);
            return null;
        }
        return blockFormatter.parse(blockEntity.getData());
    }

    @Override
    public List<Block> getBlocksByHeight(long height) {
        BlockIndex blockIndex;
        try {
            blockIndex = blockIndexService.getBlockIndexByHeight(height);
        } catch (Exception e) {
            throw new IllegalStateException("Get block index error");
        }

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

    /**
     * Steps for saving block completely:
     * 1.save block
     * 2.save block index
     * 3.save transaction index
     * 4.save utxo
     * 5.save score
     * 6.save new dpos
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Block saveBlockCompletely(Block block) throws Exception {

        //step 1
        addBlock2BlockEntity(block);


        //step 2
        boolean isFirst = hasBlockByHeight(block);
        Block toBeBestBlock = null;
        if (isFirst) {
            toBeBestBlock = getToBeBestBlock(block);
        } else {
            LOGGER.info("block:{} is not first at height :{}", block.getHash(), block.getHeight());
        }
        //step 2 whether this block can be confirmed pre N block
        blockIndexService.addBlockIndex(block, toBeBestBlock);

        if (block.isGenesisBlock()) {
            //step 3
            minerScoreStrategy.refreshMinersScore(block);
            //step 4
            nodeProcessor.calcNextDposNodes(block, block.getHeight());
        } else {
            if (isFirst && toBeBestBlock != null) {
                minerScoreStrategy.refreshMinersScore(toBeBestBlock);
                List<String> nextDposAddressList = nodeProcessor.calcNextDposNodes(toBeBestBlock, block.getHeight());
                minerScoreStrategy.setSelectedDposScore(nextDposAddressList);
                //step5
                freshPeerMinerAddr(toBeBestBlock);
            }
        }

        return toBeBestBlock;
    }

    /**
     * fresh peer's minerAddress to connect ahead
     *
     * @param toBeBestBlock
     */
    private void freshPeerMinerAddr(Block toBeBestBlock) {
        List<String> dposGroupBySn = new LinkedList<>();
        long sn = nodeProcessor.getSn(toBeBestBlock.getHeight());
        List<String> dpos = nodeProcessor.getDposGroupBySn(sn);
        if (!CollectionUtils.isEmpty(dpos)) {
            dposGroupBySn.addAll(dpos);
        }
        dpos = nodeProcessor.getDposGroupBySn(sn + 1);
        if (!CollectionUtils.isEmpty(dpos)) {
            dposGroupBySn.addAll(dpos);
        }
        peerManager.setMinerAddresses(dposGroupBySn);
    }


    @Override
    public boolean checkBlockNumbers() {
        //TODO: zhao xiaogang  should optimize  2018-05-22
        return true;
    }

    public void refreshCache(String blockHash, Block block) {
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

    private void addBlock2BlockEntity(Block block) {
        BlockEntity blockEntity = new BlockEntity();
        blockEntity.setBlockHash(block.getHash());
        blockEntity.setHeight(block.getHeight());
        blockEntity.setData(blockFormatter.format(block));
        blockRepository.save(blockEntity);
        LOGGER.info("saved block:{}", block.getSimpleInfo());
    }


    private boolean hasBlockByHeight(Block block) {
        if (block.isGenesisBlock()) {
            return true;
        }
        return null == blockIndexService.getBlockIndexByHeight(block.getHeight());
    }


    private Block getToBeBestBlock(Block block) {
        if (block.isGenesisBlock()) {
            return null;
        }
        if (block.getHeight() - NodeProcessor.CONFIRM_BEST_BLOCK_MIN_NUM < MAIN_CHAIN_START_HEIGHT) {
            return null;
        }
        Block bestBlock = recursePreBlock(block.getPrevBlockHash(), NodeProcessor.CONFIRM_BEST_BLOCK_MIN_NUM);
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

}
