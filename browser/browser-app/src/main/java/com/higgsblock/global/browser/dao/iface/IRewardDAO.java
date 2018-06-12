package com.higgsblock.global.browser.dao.iface;

import com.higgsblock.global.browser.dao.entity.RewardPO;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-24
 */
public interface IRewardDAO extends IDAO<RewardPO> {
    /**
     * batch insert
     *
     * @param rewardPos
     * @return
     */
    int[] batchInsert(List<RewardPO> rewardPos);

    /**
     * get the node's reward by address
     * @param address
     * @param start
     * @param limit
     * @return
     */
    List<RewardPO> findByPage(String address, int start, int limit);

    /**
     * count number by address
     * @param address
     * @return
     */
    long countByAddress(String address);
}
