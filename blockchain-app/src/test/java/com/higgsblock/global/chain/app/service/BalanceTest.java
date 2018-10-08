package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.dao.IBalanceRepository;
import com.higgsblock.global.chain.common.utils.Money;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

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
        balanceRepository.deleteAll();
        Map<String,Money> maps = balanceService.get("1CYx4DTTFLRGMNKLM3CNynbCamhUgcomRf");

        List<Block> blocks = blockService.getBlocksByHeight(1);

        Map<String,Money> maps2 = balanceService.get("1CYx4DTTFLRGMNKLM3CNynbCamhUgcomRf");

        int a = 0;
    }
}