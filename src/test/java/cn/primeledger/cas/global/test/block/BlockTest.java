package cn.primeledger.cas.global.test.block;

import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.test.BaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.DB;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yangyi
 * @deta 2018/2/26
 * @description
 */
public class BlockTest extends BaseTest {

    @Autowired
    private DB blockChainDB;

    @Autowired
    private BlockService blockService;

    @Before
    public void initData() {
        if (blockChainDB == null) {
            System.out.print("db is no");
            System.exit(-1);
        }
        blockService.genesisBlock();
    }

    @Test
    public void test() {
    }

    @After
    public void after() {
        blockChainDB.close();
    }

}
