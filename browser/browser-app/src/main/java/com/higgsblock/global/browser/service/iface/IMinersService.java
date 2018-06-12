package com.higgsblock.global.browser.service.iface;

import com.higgsblock.global.browser.dao.entity.MinerPO;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-21
 */
public interface IMinersService {
    /**
     * Add data to the tables specified by the database.
     *
     * @param minerPo entity
     * @return
     */
    int add(MinerPO minerPo);

    /**
     * Update the data for the database specified tables.
     *
     * @param minerPo entity
     * @return
     */
    int update(MinerPO minerPo);

    /**
     * Deletes the contents of the database specified table.
     *
     * @param address
     * @return
     */
    int delete(String address);

    /**
     * Specify the contents of the table according to the field query database.
     *
     * @param address
     * @return
     */
    MinerPO getByField(String address);

    /**
     * Get the total number of miners.
     *
     * @return
     */
    long getMinersCount();

    /**
     * Paging query data for the specified table.
     *
     * @param start
     * @param limit
     * @return
     */
    List<MinerPO> findByPage(Integer start, Integer limit);

    /**
     * Check if the address is a miners.
     *
     * @param address
     * @return
     */
    boolean isMiner(String address);

    /**
     * Add data to the tables specified by the database.
     *
     * @param miners entity
     * @return
     */
    void batchSaveOrUpdate(List<MinerPO> miners);

    /**
     * delete miners by address
     *
     * @param address
     * @return
     */
    void batchDeleteMiners(List<String> address);

}
