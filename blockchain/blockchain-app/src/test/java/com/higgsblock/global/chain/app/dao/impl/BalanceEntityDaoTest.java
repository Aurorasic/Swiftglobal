package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.dao.iface.IBalanceEntity;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.app.service.impl.TransDaoService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class BalanceEntityDaoTest extends BaseTest {
    @Autowired
    private IBalanceEntity balanceDao;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransDaoService transDaoService;

    @Test
    public void add() throws Exception {
        balanceTest();
//        boolean rs = transactionService.hasMinerStake("2222");
//        Assert.assertNotEquals(false, rs);
    }

    private void addTest() {
        BalanceEntity entity = new BalanceEntity("222", "10", "miner");
        int rs = balanceDao.add(entity);
        boolean r = rs > 0;
    }


    private void updateTest() {
        int us = balanceDao.update(new BalanceEntity("111", "5", "miner"));
        boolean r = us == 1;
    }

    private void deleteTest() {
        int rs = balanceDao.delete("000");
        boolean r = rs == 1;
    }

    private void findAllTest() {
        BalanceEntity list = balanceDao.getByField("222");
        Money mm = list.getMoney();
        Assert.assertNotEquals(null, list);
    }

    private void balanceTest() {
        Block block = buildBlock();
        transDaoService.computeMinerBalance(block);
    }

    private Block buildBlock() {
        Block block = new Block();
        Transaction tx = new Transaction();


        TransactionInput txInput = new TransactionInput();
        TransactionOutPoint txOutPoint = new TransactionOutPoint();
        txOutPoint.setHash("6dfbd9bc0a36dad5617f7326ab5bc8df41043bb8e6104ce359e3a0a9b8a5d2f1");
        txOutPoint.setIndex((short) 1);
        txInput.setPrevOut(txOutPoint);
        tx.setInputs(Lists.newArrayList(txInput));

        TransactionOutput txOutput = new TransactionOutput();
        txOutput.setMoney(new Money(1, SystemCurrencyEnum.MINER.getCurrency()));
        LockScript ls = new LockScript();
        ls.setType((short) 1);
        ls.setAddress("333");
        txOutput.setLockScript(ls);

        TransactionOutput txOutput2 = new TransactionOutput();
        txOutput2.setMoney(new Money(1000000000 - 1, SystemCurrencyEnum.MINER.getCurrency()));
        LockScript ls2 = new LockScript();
        ls2.setType((short) 1);
        ls2.setAddress("1HJQyN7q4qsXczkzwQkeon3S6YMixk1v82");
        txOutput2.setLockScript(ls2);

        tx.setOutputs(Lists.newArrayList(txOutput, txOutput2));

        block.setTransactions(Lists.newArrayList(tx));

        return block;
    }

}