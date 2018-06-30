package com.higgsblock.global.chain.app.dao.iface;


import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-08
 */
public interface IBlockIndexEntity extends IDao<BlockIndexEntity> {

    /**
     * Obtain BlockIndex according to height.
     *
     * @param height
     * @return
     */
    List<BlockIndexEntity> getAllByHeight(long height);

    /**
     * Get the highest height BlockIndex.
     *
     * @return
     */
    long getMaxHeight();

    /**
     * batch insert
     *
     * @param blockIndexEntities
     * @return
     */
    int[] insertBatch(List<BlockIndexEntity> blockIndexEntities);

}
