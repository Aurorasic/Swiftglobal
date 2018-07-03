package com.higgsblock.global.chain.app.dao.impl;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.dao.iface.IBalanceEntity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class BalanceEntityDaoTest extends BaseTest{
    @Autowired
    private IBalanceEntity balanceDao;

    @Test
    public void add() throws Exception {
        BalanceEntity entity = new BalanceEntity("111", "1", "miner");
        int rs = balanceDao.add(entity);
        boolean r = rs > 0;
    }

}