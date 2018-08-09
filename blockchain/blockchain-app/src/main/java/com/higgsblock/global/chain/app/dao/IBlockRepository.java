package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.blockchain.Block;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-08-08
 */
public interface IBlockRepository {

    boolean save(Block block);

    List<Block> findByHeight(long height);

    boolean deleteByHeight(long height);
}
