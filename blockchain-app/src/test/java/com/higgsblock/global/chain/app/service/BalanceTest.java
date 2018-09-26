package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.dao.IBalanceRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author yanghuadong
 * @date 2018-09-26
 */
public class BalanceTest extends BaseTest {
    @Autowired
    private IBalanceService balanceService;

    @Autowired
    private IBalanceRepository balanceRepository;

    @Autowired
    private IBlockService blockService;

    @Test
    public void saveTest(){
        //BalanceEntity balanceEntity = balanceRepository.save(new BalanceEntity("AAAA", Arrays.asList(new Money(1, "miner"), new Money(10, "cas"))));

        //Money money = balanceService.getBalanceOnBest("AAAA", "miner");

        List<Block> blocks = blockService.getBlocksByHeight(1);
        balanceService.save(blocks.get(0));
    }
}