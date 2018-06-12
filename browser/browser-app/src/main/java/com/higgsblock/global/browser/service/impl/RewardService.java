package com.higgsblock.global.browser.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.browser.dao.entity.RewardPO;
import com.higgsblock.global.browser.dao.iface.IRewardDAO;
import com.higgsblock.global.browser.service.bo.BlockHeaderBO;
import com.higgsblock.global.browser.service.bo.PageEntityBO;
import com.higgsblock.global.browser.service.bo.RewardBlockBO;
import com.higgsblock.global.browser.service.iface.IBlockHeaderService;
import com.higgsblock.global.browser.service.iface.IRewardService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Statement;
import java.util.List;

import static com.higgsblock.global.browser.service.bo.PageEntityBO.createBuilder;

/**
 * @author Su Jiulong
 * @date 2018-05-24
 */
@Service
@Slf4j
public class RewardService implements IRewardService {

    @Autowired
    private IRewardDAO iRewardDao;

    @Autowired
    private IBlockHeaderService iBlockHeaderService;

    @Override
    public void batchInsert(List<RewardPO> rewards) {
        if (CollectionUtils.isEmpty(rewards)) {
            LOGGER.warn("reward list is empty");
            return;
        }

        int[] ints = iRewardDao.batchInsert(rewards);
        for (int anInt : ints) {
            if (anInt < 0 && anInt != Statement.SUCCESS_NO_INFO) {
                LOGGER.error("reward batchInsert result error");
                throw new RuntimeException("reward batchInsert result error ");
            }
        }
        LOGGER.info("reward batchInsert success");
    }

    @Override
    public PageEntityBO<RewardBlockBO> findByPage(String address, Integer start, Integer limit) {
        if (StringUtils.isEmpty(address)) {
            LOGGER.error("the address is empty");
            return null;
        }

        //Let's start with that one
        int begin = (start - 1) * limit;
        //Get the required data
        List<RewardPO> bRewards = iRewardDao.findByPage(address, begin, limit);
        return rewardPOs2RewardBlockBOs(address, bRewards);
    }

    @Override
    public PageEntityBO<RewardBlockBO> getRewardBlocks(String address) {
        if (StringUtils.isEmpty(address)) {
            LOGGER.error("the address is empty");
            return null;
        }
        List<RewardPO> bRewards = iRewardDao.getByField(address);
        return rewardPOs2RewardBlockBOs(address, bRewards);
    }

    private PageEntityBO<RewardBlockBO> rewardPOs2RewardBlockBOs(String address, List<RewardPO> bRewards) {
        //Build the results and return
        if (CollectionUtils.isEmpty(bRewards)) {
            LOGGER.error("Find by page dataList empty");
            return null;
        }
        //Gets the total number of block headers in the table
        long totalCount = iRewardDao.countByAddress(address);
        if (totalCount == 0) {
            LOGGER.error("The record that the database satisfies the condition is = {} ", totalCount);
            return null;
        }
        List<RewardBlockBO> rewardBlockBos = Lists.newArrayList();
        bRewards.forEach(bReward -> {
            RewardBlockBO rewardBlockBo = new RewardBlockBO();
            rewardBlockBo.setEarnings(bReward.getAmount());
            rewardBlockBo.setCurrency(bReward.getCurrency());
            rewardBlockBo.setHeight(bReward.getHeight());

            String blockHash = bReward.getBlockHash();
            rewardBlockBo.setBlockHash(blockHash);
            //The block head is obtained by block's hash, and the block's size
            // and creation time are obtained by block head
            BlockHeaderBO blockHeaderBo = iBlockHeaderService.getByField(blockHash);
            rewardBlockBo.setSize(blockHeaderBo.getBlockSize());
            rewardBlockBo.setCreateAt(blockHeaderBo.getBlockTime());
            rewardBlockBos.add(rewardBlockBo);
        });
        return createBuilder().withItmes(rewardBlockBos).withTotal(totalCount).builder();
    }
}
