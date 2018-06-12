package com.higgsblock.global.browser.service.impl;

import com.higgsblock.global.browser.dao.entity.MinerPO;
import com.higgsblock.global.browser.dao.iface.IMinersDAO;
import com.higgsblock.global.browser.service.iface.IMinersService;
import com.higgsblock.global.browser.utils.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Statement;
import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-21
 */
@Service
@Slf4j
public class MinersService implements IMinersService {
    @Autowired
    private IMinersDAO iMinersDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(MinerPO minerPo) {
        return iMinersDao.add(minerPo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(MinerPO minerPo) {
        return iMinersDao.update(minerPo);
    }

    @Override
    public int delete(String address) {
        return iMinersDao.delete(address);
    }

    @Override
    public MinerPO getByField(String address) {
        if (StringUtils.isEmpty(address)) {
            LOGGER.error("address is empty");
        }
        List<MinerPO> miners = iMinersDao.getByField(address);
        if (CollectionUtils.isEmpty(miners)) {
            LOGGER.error("find miner by address return empty,address = {}", address);
            return null;
        }
        return miners.get(0);
    }

    @Override
    public long getMinersCount() {
        return iMinersDao.getMinersCount();
    }

    @Override
    public List<MinerPO> findByPage(Integer start, Integer limit) {
        return iMinersDao.findByPage(start, limit);
    }

    @Override
    public boolean isMiner(String address) {
        if (StringUtils.isEmpty(address)) {
            LOGGER.error("isMiner address empty");
            return false;
        }

        MinerPO miner = getByField(address);
        if (miner == null) {
            return false;
        }

        Money targetMoney = new Money(1L);
        Money money = new Money(miner.getAmount());

        return money.compareTo(targetMoney) >= 0;
    }

    @Override
    public void batchSaveOrUpdate(List<MinerPO> miners) {
        if (CollectionUtils.isEmpty(miners)) {
            LOGGER.warn("miners list is empty");
            return;
        }
        int[] ints = iMinersDao.batchSaveOrUpdate(miners);
        for (int anInt : ints) {
            if (anInt < 0 && anInt != Statement.SUCCESS_NO_INFO) {
                LOGGER.error("miners batchSaveOrUpdate result error");
                throw new RuntimeException("miners batchSaveOrUpdate error");
            }
        }
        LOGGER.info("miners batchSaveOrUpdate success");
    }

    @Override
    public void batchDeleteMiners(List<String> address) {
        if (CollectionUtils.isEmpty(address)) {
            LOGGER.error("delete miners by address error, because address is empty");
            return;
        }
        int[] results = iMinersDao.batchDeleteMiners(address);

        if (results.length == 1 && results[0] == 0) {
            LOGGER.error("delete miners by address result error   address= {}", address);
            throw new RuntimeException("delete miners by address error");
        }
        LOGGER.info("delete miners by address success  address = {}", address);
    }
}
