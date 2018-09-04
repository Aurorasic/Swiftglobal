package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;

import java.util.List;

/**
 * @author yangyi
 * @deta 2018/5/24
 * @description
 */
public interface IDposService {


    /**
     * the selected miners per round
     */
    int NODE_SIZE = 7;
    /**
     * the max size of cache nodes
     */
    int MAX_SIZE = 30;
    /**
     * the best block confirmed which the height of go back 3
     */
    int CONFIRM_BEST_BLOCK_MIN_NUM = 3;
    /**
     * the blocks per round
     */
    int DPOS_BLOCKS_PER_ROUND = 5;
    /**
     * start height
     */
    long DPOS_START_HEIGHT = 2L;

    /**
     * get dpos addresses by serial number
     *
     * @param sn the serial number
     * @return the dpos address list
     */
    List<String> get(long sn);

    /**
     * save the dpos addresses into local database while the key is sn
     *
     * @param sn        the key of the dpos addresses in local database
     * @param addresses the dpos address list
     */
    void save(long sn, List<String> addresses);

    /**
     * calculate next round lucky miners and persist
     *
     * @param toBeBestBlock
     * @param maxHeight
     * @return
     */
    List<String> calcNextDposNodes(Block toBeBestBlock, long maxHeight);


    /**
     * find lucky miners address by round num
     *
     * @param sn
     * @return
     */
    List<String> getDposGroupBySn(long sn);

    /**
     * find lucky miners by pre blockhash
     *
     * @param preBlockHash
     * @return
     */
    List<String> getRestDposMinersByPreHash(String preBlockHash);

    /**
     * validate the producer
     *
     * @param block
     * @return
     */
    boolean checkProducer(Block block);

    /**
     * check the miner should be lucky miner or not
     *
     * @param height
     * @param address
     * @param preBlockHash
     * @return
     */
    boolean canPackBlock(long height, String address, String preBlockHash);

    /**
     * calculate the start height in this round of the height
     *
     * @param height
     * @return
     */
    long calculateStartHeight(long height);

    /**
     * calculate the end height in this round of the height
     *
     * @param height
     * @return
     */
    long calculateEndHeight(long height);

    /**
     * calculate the round num by height
     *
     * @param height
     * @return
     */
    long calculateSn(long height);

    /**
     * check block unstrictly,if the miner is constained in the dpos miners,return true
     *
     * @param block
     * @return
     */
    boolean checkBlockUnstrictly(Block block);
}
