package com.higgsblock.global.browser.dao.iface;

import com.higgsblock.global.browser.dao.entity.BlockHeaderPO;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-21
 */
public interface IBlockHeaderDAO extends IDAO<BlockHeaderPO> {

    public static final String ORDER_BY_DESC = "DESC";

    public static final String ORDER_BY_ASC = "ASC";

    /**
     * get max height of block
     *
     * @return
     */
    long getMaxHeight();

    /**
     * batch insert
     *
     * @param blockHeaderPos
     * @return
     */
    int[] batchInsert(List<BlockHeaderPO> blockHeaderPos);

    /**
     * Gets the total number of records in the area in the table.
     *
     * @return
     */
    long getAllBlockHeaderSize();

    /**
     * Get the latest block.
     *
     * @param start
     * @param limit
     * @param orderBy
     * @return
     */
    List<BlockHeaderPO> getScopeBlock(long start, long limit, String orderBy);
}
