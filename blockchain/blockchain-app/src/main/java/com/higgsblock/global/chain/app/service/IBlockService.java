package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;

import java.util.List;

/**
 * @author Zhao xiaogang
 * @date 2018-05-21
 */
public interface IBlockService {

    /**
     * Check if the block with this block hash exist in the database
     *
     * @param height    block height
     * @param blockHash the block hash
     * @return boolean
     */
    boolean isExistInDB(long height, String blockHash);

    /**
     * Check if the block exist in the database and orphan block cache
     *
     * @param block the block
     * @return boolean
     */
    boolean isExist(Block block);

    /**
     * Check if the pre-block exist in the database
     *
     * @param block the block
     * @return boolean
     */
    boolean preIsExistInDB(Block block);

    /**
     * Get block by block hash
     *
     * @param blockHash the block hash
     * @return Block
     */
    Block getBlockByHash(String blockHash);


    /**
     * Get blocks by height
     *
     * @param height height
     * @return List<Block>
     */
    List<Block> getBlocksByHeight(long height);

    /**
     * Get blocks except the block with the height and the block hash
     *
     * @param height          the height
     * @param exceptBlockHash the excluding block hash
     * @return List<Block>
     */
    List<Block> getBlocksExcept(long height, String exceptBlockHash);

    /**
     * Get the best block by height
     *
     * @param height height
     * @return Block
     */
    Block getBestBlockByHeight(long height);

    /**
     * Save the block, block index , transaction index, utxo and scores, all of relation of block.
     * @param block
     * @param sourceId
     * @return
     */
    boolean persistBlockAndIndex(Block block, String sourceId);

    /**
     * Check the block numbers
     *
     * @return boolean
     */
    boolean checkBlockNumbers();

    /**
     * whether the block is first block at its height
     *
     * @param block
     * @return
     */
    boolean isFirstBlockByHeight(Block block);

    /**
     * find the best block (h-N) when a block is confirmed
     *
     * @param block
     * @return
     */
    Block getToBeBestBlock(Block block);

    /**
     * get last best BlockIndex
     *
     * @return the last best blockIndex
     */
    BlockIndex getLastBestBlockIndex();

    /**
     * Check witness signatures
     *
     * @param block the block
     * @return the boolean
     */
    boolean checkWitnessSignatures(Block block);

    /**
     * Check the producer of the block.
     *
     * @param block
     * @return the boolean
     */
    boolean checkDposProducerPermission(Block block);

    /**
     * packageNewBlock
     *
     * @param preBlockHash
     * @return
     */
    Block packageNewBlock(String preBlockHash);
}