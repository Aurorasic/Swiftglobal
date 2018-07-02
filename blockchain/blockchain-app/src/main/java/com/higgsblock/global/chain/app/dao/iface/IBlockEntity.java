package com.higgsblock.global.chain.app.dao.iface;


import com.higgsblock.global.chain.app.dao.entity.BlockEntity;

/**
 * Extension block basic functionality.
 *
 * @author yangshenghong
 * @date 2018-05-08
 */
public interface IBlockEntity extends IDao<BlockEntity> {
    /**
     * Gets the total number of database blocks.
     *
     * @return
     */
    long getBlockCount();


}
