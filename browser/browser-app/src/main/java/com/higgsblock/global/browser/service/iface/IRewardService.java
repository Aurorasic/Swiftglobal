package com.higgsblock.global.browser.service.iface;

import com.higgsblock.global.browser.dao.entity.RewardPO;
import com.higgsblock.global.browser.service.bo.PageEntityBO;
import com.higgsblock.global.browser.service.bo.RewardBlockBO;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-24
 */
public interface IRewardService {

    /**
     * batch insert
     *
     * @param rewardPos
     */
    void batchInsert(List<RewardPO> rewardPos);

    /**
     * get the node's reward by address
     *
     * @param address
     * @param start
     * @param limit
     * @return
     */
    PageEntityBO<RewardBlockBO> findByPage(String address, Integer start, Integer limit);

    /**
     * get rewardBlocks by address
     *
     * @param address
     * @return
     */
    PageEntityBO<RewardBlockBO> getRewardBlocks(String address);
}
