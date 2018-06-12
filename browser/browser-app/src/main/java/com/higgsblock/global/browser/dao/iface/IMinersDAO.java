package com.higgsblock.global.browser.dao.iface;

import com.higgsblock.global.browser.dao.entity.MinerPO;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-21
 */
public interface IMinersDAO extends IDAO<MinerPO> {
    /**
     * Get the total number of miners.
     *
     * @return
     */
    long getMinersCount();

    /**
     * batch insert
     *
     * @param miners
     * @return
     */
    int[] batchInsert(List<MinerPO> miners);

    /**
     * Add data to the tables specified by the database.
     *
     * @param miners
     * @return
     */
    int[] batchSaveOrUpdate(List<MinerPO> miners);

    /**
     * delete miners by address
     * @param address
     * @return
     */
    int[] batchDeleteMiners(List<String> address);
}
