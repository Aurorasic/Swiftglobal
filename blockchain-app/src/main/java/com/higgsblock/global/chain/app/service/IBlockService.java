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
     * Get the best block by height
     *
     * @param height height
     * @return Block
     */
    Block getBestBlockByHeight(long height);

    /**
     * Save the block, block index , transaction index, utxo and scores, all of relation of block.
     *
     * @param block
     * @return
     */
    Block persistBlockAndIndex(Block block);

    /**
     * Do some sync work(should not be async work) after persisted a block
     *
     * @param newBestBlock
     * @param persistedBlock
     */
    void doSyncWorksAfterPersistBlock(Block newBestBlock, Block persistedBlock);

    int deleteByHeight(long height);

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