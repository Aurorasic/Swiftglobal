package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yangyi
 * @deta 2018/7/3
 * @description
 */
public class WitnessServiceTest extends BaseTest {

    @Autowired
    private WitnessService witnessService;

    @Test
    public void test() {
        witnessService.initWitnessTask(99);
        Block block = new Block();
        block.setHeight(99);
        block.setHash("111111");
        witnessService.addSourceBlock(block);
        block.setHeight(99);
        block.setHash("22222");
        witnessService.addSourceBlock(block);
        block.setHeight(100);
        block.setHash("33333");
        witnessService.addSourceBlock(block);
        block.setHeight(100);
        block.setHash("44444");
        witnessService.addSourceBlock(block);
        witnessService.initWitnessTask(100);
        block.setHeight(100);
        block.setHash("55555");
        witnessService.addSourceBlock(block);
    }
}
