package com.higgsblock.global.chain.app.blockchain;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-07-20
 */
public interface IBlockChain {

    /**
     *
     * @param address
     * @param height
     * @return
     */
    boolean isLuckyMiner(String address, long height);

    boolean isMiner(String address, long height);

    boolean isWitness(String address, long height);

    boolean isGuarder(String address, long height);

    boolean isGenesisBlock(Block block);

    boolean isGenesisBlock(String blockHash);

    boolean isExistPreBlock(String blockHash);

    boolean isExistBlock(String blockHash);

    boolean checkBlockBasicInfo(Block block);

    boolean checkTransactions(Block block);

    boolean checkWitnessSignature(Block block);

    boolean checkBlockProducer(Block block);

    long getMaxHeight();

    long getBestMaxHeight();

    Block getBlock(String blockHash);

    List<Block> getBlocks(long height);

    BlockIndex getBlockIndex(long height);

    List<Block> getHeightestBlocks();

}
