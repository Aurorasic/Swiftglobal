package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;

import java.util.List;

/**
 * The interface Block index service.
 *
 * @author Zhao xiaogang
 * @date 2018 -05-21
 */
public interface IBlockIndexService {
    /**
     * get block index by height
     *
     * @param height the block height
     * @return BlockIndex block index by height
     */
    BlockIndex getBlockIndexByHeight(long height);

    /**
     * fetch data by rule id
     *
     * @param block     the block
     * @param bestBlock best block hash
     * @return BlockIndexDaoEntity
     */
    void addBlockIndex(Block block, Block bestBlock);

    /**
     * Gets last block index.
     *
     * @return the last block index
     */
    BlockIndex getLastBlockIndex();

    /**
     * Gets last height block hashs.
     *
     * @return the last height block hashs
     */
    List<String> getLastHeightBlockHashs();
}
