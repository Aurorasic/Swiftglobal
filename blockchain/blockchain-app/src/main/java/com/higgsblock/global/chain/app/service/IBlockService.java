package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;

import java.util.List;

/**
 * The interface Block service.
 *
 * @author Zhao xiaogang
 * @date 2018 -05-21
 */
public interface IBlockService {

    /**
     * Check if the block with this block hash exist in the database
     *
     * @param height    block height
     * @param blockHash the block hash
     * @return boolean boolean
     */
    boolean isExistInDB(long height, String blockHash);

    /**
     * Check if the block exist in the database
     *
     * @param block the block
     * @return boolean boolean
     */
    boolean isExist(Block block);

    /**
     * Check if the pre-block exist in the database
     *
     * @param block the block
     * @return boolean boolean
     */
    boolean preIsExistInDB(Block block);

    /**
     * Get block by block hash
     *
     * @param blockHash the block hash
     * @return Block block by hash
     */
    Block getBlockByHash(String blockHash);


    /**
     * Get blocks by height
     *
     * @param height height
     * @return List<Block>  blocks by height
     */
    List<Block> getBlocksByHeight(long height);

    /**
     * Get blocks except the block with the height and the block hash
     *
     * @param height          the height
     * @param exceptBlockHash the excluding block hash
     * @return List<Block>  blocks except
     */
    List<Block> getBlocksExcept(long height, String exceptBlockHash);

    /**
     * Get the best block by height
     *
     * @param height height
     * @return Block best block by height
     */
    Block getBestBlockByHeight(long height);

    /**
     * Save the block, block index , transaction index, utxo and scores
     *
     * @param block the block
     * @return block
     * @throws Exception the exception
     */
    void saveBlock(Block block) throws Exception;

    /**
     * Check the block numbers
     *
     * @return boolean boolean
     */
    boolean checkBlockNumbers();

    /**
     * whether the block is first block at its height
     *
     * @param block the block
     * @return boolean boolean
     */
    boolean isFirstBlockByHeight(Block block);

    /**
     * find the best block (h-N) when a block is confirmed
     *
     * @param block the block
     * @return to be best block
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
     * Persist block and index
     *
     * @param block   the block
     * @param version the version
     * @return the boolean
     */
    boolean persistBlockAndIndex(Block block, int version);

}
