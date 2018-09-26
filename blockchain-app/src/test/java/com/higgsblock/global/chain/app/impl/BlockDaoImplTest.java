package com.higgsblock.global.chain.app.impl;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        block.setHash("ec5ab00db4329ba7671cd087f5b31b540e98dc636b8ba90776abfb920d9a174f");
        block.setPrevBlockHash("9469c4b8e890d6e089f235fd0df43e8c62e1461771abdb8f6569784499736f82");
//        Block bestBlock = blockDaoService.getToBeBestBlock(block);
    }

    private void testGetBestBlockIndex(long height) {
        System.out.println(blockService.getBestBlockByHeight(height));

    }

    @Test
    public void saveAndSelectBlock() {
        Block block = new Block();
        block.setVersion((short) 1);
        block.setBlockTime(System.currentTimeMillis());
        block.setPrevBlockHash(null);
        block.setTransactions(new LinkedList<>());
        block.setHeight(1);
        block.setMinerPubKey("02ca7d48b0a27f7d996839cbc4b0efa4722a1c61331061de691c3bcbbb74c2fa7d");
        String sig = ECKey.signMessage(block.getHash(), "e45a782fb642f355772c4b6b4a93c008c4fbf2202c56ef46c208f87f22a11a98");
        block.setMinerSignature(sig);
        blockService.saveBlockCompletely(block);
        blockService.getBlockByHash("abc");
        blockService.getBlockByHash("abc");
        blockService.getBlockByHash("abce");
        blockService.getBlockByHash("abc");
        blockService.getBlockByHash("abce");
        blockService.getBlockByHash("abc");
        blockService.getBlockByHash("abce");
    }
    
    @Test
    public void testSaveBlocksConcurrent() {
        ExecutorService exec = Executors.newFixedThreadPool(15);
        for (long i = 2; i < 2000; i++) {
            final long height = i;
            exec.submit(() -> saveBlock(height));
        }
    }

    private void saveBlock(long height) {
        Block block = new Block();
        block.setHeight(height);
        block.setHash(String.valueOf(height));
        blockService.getBlocksByHeight(height - 1);
        try {
            Thread.sleep(1000);
            blockService.saveBlock(block);
            Thread.sleep(1000);
        } catch (Exception e) {
            LOGGER.error("Exception," + block.getSimpleInfo(), e);
        }

        blockService.getBlocksByHeight(height - 1);
    }
}
