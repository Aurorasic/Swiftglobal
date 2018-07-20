package com.higgsblock.global.chain.app.blockchain;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-07-20
 */
public interface IBlockChainService {

    /**
     * Return true if the address belongs to a lucky miner at the specified height.
     * Lucky miner can produce a block at a specified height.
     *
     * @param address
     * @param preBlockHash
     * @return
     */
    boolean isLuckyMiner(String address, String preBlockHash);

    /**
     * Return true if the address belongs to a miner at the specified height.
     * All miners have a chance to be lucky miner.
     *
     * @param address
     * @return
     */
    boolean isMinerOnBest(String address);

    /**
     * Return true if the address belongs to a witness at the specified height.
     * All witnesses together choose a correct block from multiple blocks at the same height.
     *
     * @param address
     * @param height
     * @return
     */
    boolean isWitness(String address, long height);

    /**
     * Return true if the address belongs to a guarder at the specified height.
     * Guarders can produce blocks if all lucky miners can not produce a correct block at the specified height.
     *
     * @param address
     * @param preBlockHash
     * @return
     */
    boolean isGuarder(String address, String preBlockHash);

    /**
     * Return true if the block is the genesis block on the chain.
     *
     * @param block
     * @return
     */
    boolean isGenesisBlock(Block block);

    /**
     * Return true if the block is the genesis block on the chain.
     *
     * @param blockHash
     * @return
     */
    boolean isGenesisBlock(String blockHash);

    /**
     * Return true if the prev block is already on the chain.
     *
     * @param blockHash
     * @return
     */
    boolean isExistPreBlock(String blockHash);

    /**
     * Return true if the block is already on the chain.
     *
     * @param blockHash
     * @return
     */
    boolean isExistBlock(String blockHash);

    /**
     * @param block
     * @return
     */
    boolean checkBlockBasicInfo(Block block);

    /**
     * Check all transactions in the block.
     *
     * @param block
     * @return
     */
    boolean checkTransactions(Block block);

    /**
     * Check the signature of all witnesses.
     *
     * @param block
     * @return
     */
    boolean checkWitnessSignature(Block block);

    /**
     * Check the producer of the block.
     *
     * @param block
     * @return
     */
    boolean checkBlockProducer(Block block);

    /**
     * Get the max height of the block chain, it may not be the best.
     *
     * @return
     */
    long getMaxHeight();

    /**
     * Get the max height of the block chain, it must be the best.
     *
     * @return
     */
    long getBestMaxHeight();

    /**
     * Get block by hash.
     *
     * @param blockHash
     * @return
     */
    Block getBlock(String blockHash);

    /**
     * Get blocks by height.
     *
     * @param height
     * @return
     */
    List<Block> getBlocks(long height);

    /**
     * Get block index by height;
     *
     * @param height
     * @return
     */
    BlockIndex getBlockIndex(long height);

    /**
     * Get all the Highest blocks.
     *
     * @return
     */
    List<Block> getHighestBlocks();

}
