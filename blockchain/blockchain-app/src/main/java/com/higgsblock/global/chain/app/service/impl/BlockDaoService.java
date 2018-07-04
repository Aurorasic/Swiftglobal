package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.formatter.BlockFormatter;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionCacheManager;
import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.consensus.MinerScoreStrategy;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.dao.impl.BlockEntityDao;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Zhao xiaogang
 * @date 2018-05-21
 */
@Service
@Slf4j
public class BlockDaoService implements IBlockService, InitializingBean {

    @Autowired
    private BlockEntityDao blockDao;

    @Autowired
    private BlockCacheManager blockCacheManager;

    @Autowired
    private BlockIdxDaoService blockIdxDaoService;

    @Autowired
    private TransDaoService transDaoService;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private PeerManager peerManager;

    @Autowired
    private BlockFormatter blockFormatter;
    @Autowired
    private AppConfig config;

    private int confirmPreHeightNum;

    private static final int MAIN_CHAIN_START_HEIGHT = 2;


    @Override
    public void afterPropertiesSet() {
        confirmPreHeightNum = config.getBestchainConfirmNum();
    }

    @Override
    public boolean isExistInDB(long height, String blockHash) {

        BlockIndex blockIndex = blockIdxDaoService.getBlockIndexByHeight(height);
        return null != blockIndex
                && null != blockIndex.getBestBlockHash()
                && blockIndex.getBestBlockHash().equals(blockHash);

    }

    @Override
    public boolean isExist(Block block) {
        BlockIndex blockIndex = blockIdxDaoService.getBlockIndexByHeight(block.getHeight());
        if (null != blockIndex
                && null != blockIndex.getBestBlockHash()
                && blockIndex.getBestBlockHash().equals(block.getHash())) {
            return true;
        }

        if (blockCacheManager.isContains(block.getHash())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean preIsExistInDB(Block block) {
        if (block == null) {
            return false;
        }
        return blockDao.getByField(block.getPrevBlockHash()) != null;
    }

    @Override
    public Block getBlockByHash(String blockHash) {
        BlockEntity blockEntity = blockDao.getByField(blockHash);
        if (blockEntity == null) {
            LOGGER.error("not found the block by blockHash = " + blockHash);
            return null;
        }
        return blockFormatter.parse(blockEntity.getData());
    }

    @Override
    public List<Block> getBlocksByHeight(long height) {
        BlockIndex blockIndex;
        try {
            blockIndex = blockIdxDaoService.getBlockIndexByHeight(height);
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
        BlockIndex blockIndex = blockIdxDaoService.getBlockIndexByHeight(height);
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
        BlockIndex blockIndex = blockIdxDaoService.getBlockIndexByHeight(height);
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
     * 7.refresh cache
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
        if (isFirst) {
            blockIdxDaoService.addBlockIndex(block, toBeBestBlock);
        } else {
            blockIdxDaoService.addBlockIndex(block, null);
        }

        if (block.isGenesisBlock()) {
            //step 3
            MinerScoreStrategy.refreshMinersScore(block);
            //step 4
            nodeManager.calculateDposNodes(block, block.getHeight());
        } else {
            if (isFirst && toBeBestBlock != null) {
                MinerScoreStrategy.refreshMinersScore(toBeBestBlock);
                nodeManager.calculateDposNodes(toBeBestBlock, block.getHeight());
                //step5
                freshPeerMinerAddr(toBeBestBlock);
            }
        }
        //step 6
        refreshCache(block.getHash(), block);

        return toBeBestBlock;
    }

    @Override
    public void printAllBlockData() {
    }


    /**
     * fresh peer's minerAddress to connect ahead
     *
     * @param toBeBestBlock
     */
    private void freshPeerMinerAddr(Block toBeBestBlock) {
        List<String> dposGroupBySn = new LinkedList<>();
        long sn = nodeManager.getSn(toBeBestBlock.getHeight());
        List<String> dpos = nodeManager.getDposGroupBySn(sn);
        if (!CollectionUtils.isEmpty(dpos)) {
            dposGroupBySn.addAll(dpos);
        }
        dpos = nodeManager.getDposGroupBySn(sn + 1);
        if (!CollectionUtils.isEmpty(dpos)) {
            dposGroupBySn.addAll(dpos);
        }
        peerManager.setMinerAddresses(dposGroupBySn);
    }


    @Override
    public boolean checkBlockNumbers() {
        //               long blockIndexSize = 0L;
        //        long blockMapSize = blockDao.keys().size();
        //
        //        if (blockMapSize < 0L || blockMapSize > Long.MAX_VALUE) {
        //            LOGGER.error("blockMapSize is error blockMapSize = {}", blockMapSize);
        //            return false;
        //        }

        //TODO: zhao xiaogang  should optimize  2018-05-22

        return true;
    }

    private void refreshCache(String blockHash, Block block) {
        blockCacheManager.remove(blockHash);

        block.getTransactions().stream().forEach(tx -> {
            txCacheManager.remove(tx.getHash());
        });
    }

    private void addBlock2BlockEntity(Block block) {
        BlockEntity blockEntity = new BlockEntity();
        blockEntity.setBlockHash(block.getHash());
        blockEntity.setHeight(block.getHeight());
        blockEntity.setData(blockFormatter.format(block));
        blockDao.add(blockEntity);
    }


    private boolean hasBlockByHeight(Block block) {
        if (block.isGenesisBlock()) {
            return true;
        }
        return null == blockIdxDaoService.getBlockIndexByHeight(block.getHeight());
    }


    private Block getToBeBestBlock(Block block) {
        if (block.isGenesisBlock()) {
            return null;
        }
        if (block.getHeight() - confirmPreHeightNum < MAIN_CHAIN_START_HEIGHT) {
            return null;
        }
        Block bestBlock = recursePreBlock(block.getPrevBlockHash(), confirmPreHeightNum);
        // h-N-1 block has ready be bestchain
        Block preBestBlock = getBlockByHash(bestBlock.getPrevBlockHash());
        if (preBestBlock == null || !preBestBlock.getHash().equals(getBestBlockByHeight(preBestBlock.getHeight()))) {
            //todo business error ,failure bypass
            return null;
        }

        return bestBlock;
    }

    private Block recursePreBlock(String preBlockHash, int preHeightNum) {

        Block preBlock = getBlockByHash(preBlockHash);
        if (preBlock == null) {
            LOGGER.error("preblock is null,may be db transaction error or sync error,blockhash:{}", preBlockHash);
            throw new IllegalStateException("can not find block,blockhash:" + preBlockHash);
        }
        if (preBlock.getHash().equals(blockIdxDaoService.getBlockIndexByHeight(preBlock.getHeight()).getBestBlockHash())) {
            LOGGER.info("block[blockhash:{},height:{}]has be confirmed on best chain,skip this", preBlock.getHash(), preBlock.getHeight());
            return null;
        }
        if (preHeightNum-- > 0) {
            return recursePreBlock(preBlock.getPrevBlockHash(), preHeightNum);
        }
        return preBlock;
    }

    public int getConfirmPreHeightNum() {
        return confirmPreHeightNum;
    }

}