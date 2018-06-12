package com.higgsblock.global.browser.service.iface;

import com.higgsblock.global.browser.dao.entity.BlockHeaderPO;
import com.higgsblock.global.browser.service.bo.BlockHeaderBO;
import com.higgsblock.global.browser.service.bo.PageEntityBO;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-23
 */
public interface IBlockHeaderService {

    /**
     * get max height of block
     *
     * @return
     */
    long getMaxHeight();

    /**
     * Add data to the tables specified by the database.
     *
     * @param blockHeaderPo entity
     * @return
     */
    int add(BlockHeaderPO blockHeaderPo);

    /**
     * Update the data for the database specified tables.
     *
     * @param blockHeaderPo entity
     * @return
     */
    int update(BlockHeaderPO blockHeaderPo);

    /**
     * Deletes the contents of the database specified table.
     *
     * @param blockHash
     * @return
     */
    int delete(String blockHash);

    /**
     * Specify the contents of the table according to the field query database.
     *
     * @param blockHash
     * @return
     */
    BlockHeaderBO getByField(String blockHash);

    /**
     * Paging query data for the specified table.
     *
     * @param start
     * @param limit
     * @return
     */
    PageEntityBO<BlockHeaderBO> findScopeBlock(Long start, Long limit);

    /**
     * Get the latest block.
     *
     * @param limit
     * @return
     */
    List<BlockHeaderBO> getLatestBlock(Long limit);

    /**
     * batch insert
     *
     * @param blockHeaderPos
     * @return
     */
    void batchInsert(List<BlockHeaderPO> blockHeaderPos);
}
