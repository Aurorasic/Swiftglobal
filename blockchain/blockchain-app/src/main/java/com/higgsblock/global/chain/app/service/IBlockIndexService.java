package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexDaoEntity;

import java.util.List;

/**
 * @author Zhao xiaogang
 * @date 2018-05-21
 */
public interface IBlockIndexService {
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
     * @param block the block
     * @param bestBlockHash best block hash
     * @throws Exception
     * @return BlockIndexDaoEntity
     */
    BlockIndexDaoEntity addBlockIndex(Block block, String bestBlockHash) throws Exception;

    /**
     * get all the block index keys
     *
     * @return List<byte[]>
     */
    List<byte[]> keys();
}
