package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;

/**
 * @author Zhao xiaogang
 * @date 2018-05-21
 */
public interface IBlockIndexService {

    Long getHeightByBlockHash(String blockHash);

    /**
     * get block index by height
     *
     * @param height the block height
     * @return BlockIndex
     */
    BlockIndex getBlockIndexByHeight(long height);

    /**
     * fetch data by rule id
     *
     * @param block         the block
     * @param bestBlock best block hash
     * @return BlockIndexDaoEntity
     */
    void addBlockIndex(Block block, Block bestBlock);

    /**
     * get maxheight block index
     *
     * @return
     */
    BlockIndex getLastBlockIndex();

}
