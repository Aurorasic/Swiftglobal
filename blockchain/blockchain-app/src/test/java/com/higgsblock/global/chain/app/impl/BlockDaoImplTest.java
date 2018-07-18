package com.higgsblock.global.chain.app.impl;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author HuangShengli
 * @date 2018-05-23
 */
@Slf4j
public class BlockDaoImplTest extends BaseTest {

    @Autowired
    private BlockService blockService;

    @Test
    public void test() {
//        testGetFiled();
        testGetBestBlockIndex(40);
        testGetBestBlockIndex(153);
        testGetBestBlockIndex(248);
    }

    private void testGetFiled() {
        Block block = new Block();
        block.setHeight(7);
        //ec5ab00db4329ba7671cd087f5b31b540e98dc636b8ba90776abfb920d9a174f
        block.setHash("ec5ab00db4329ba7671cd087f5b31b540e98dc636b8ba90776abfb920d9a174f");
        block.setPrevBlockHash("9469c4b8e890d6e089f235fd0df43e8c62e1461771abdb8f6569784499736f82");
//        Block bestBlock = blockDaoService.getToBeBestBlock(block);
    }

    private void testGetBestBlockIndex(long height) {
        System.out.println(blockService.getBestBlockByHeight(height));

    }
}
