package cn.primeledger.cas.global.test.block;

import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.test.BaseTest;
import org.junit.Test;
import org.mapdb.DB;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yangyi
 * @deta 2018/2/27
 * @description
 */
public class TransactionTest extends BaseTest{

    @Autowired
    private DB blockChainDB;

    @Autowired
    private BlockService blockService;

    @Test
    public void test(){

    }


}
