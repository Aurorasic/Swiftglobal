package com.higgsblock.global.chain.vm.datasource.leveldb;

import com.higgsblock.global.chain.vm.core.AccountState;
import com.higgsblock.global.chain.vm.datasource.*;
import com.higgsblock.global.chain.vm.util.Serializers;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * @author tangkun
 * @date 2018-09-20
 */
public class LevelDbDataSourceTest {

    //一级缓存
    Source accountStateSource = new LevelDbDataSource();

    LevelDbDataSource db = new LevelDbDataSource();
    @Test
    public void testGet() throws Exception {

    }

    @Test
    public void testPut() throws Exception {
        //一级缓存
        DbSource<byte[]> dbSource = new LevelDbDataSource();
        dbSource.setName("contract1");
        dbSource.init(DbSettings.DEFAULT);
        BatchSourceWriter writer = new BatchSourceWriter<>(dbSource);
        writer.setFlushSource(true);

        SourceCodec.BytesKey<AccountState, byte[]> accountStateCodec = new SourceCodec.BytesKey<>(writer, Serializers.AccountStateSerializer);
        accountStateCodec.setFlushSource(true);
        WriteCache.BytesKey<AccountState> accountStateCache = new WriteCache.BytesKey<>(accountStateCodec, WriteCache.CacheType.SIMPLE);
        accountStateCache.setFlushSource(true);

        /**********START 账户缓存******************/
        //交易账户缓存
        Source<byte[], AccountState> txAccountStateCache = new WriteCache.BytesKey(accountStateCache,
                WriteCache.CacheType.SIMPLE);
        ((WriteCache)txAccountStateCache).setFlushSource(true);

        //合约缓存
        Source<byte[], AccountState> conAccountStateCache = new WriteCache.BytesKey(txAccountStateCache,
                WriteCache.CacheType.SIMPLE);
        ((WriteCache)conAccountStateCache).setFlushSource(true);
        byte[] key = "hello1".getBytes();
        AccountState accountState = new AccountState(0, BigInteger.valueOf(10));
        conAccountStateCache.put(key,accountState);

        /**********END 账户缓存******************/

        /*************START code缓存*******************/
        Source<byte[], byte[]> codeCache = new WriteCache.BytesKey<>(writer, WriteCache.CacheType.COUNTING);
        ((WriteCache)codeCache).setFlushSource(true);
        System.out.println(new String(codeCache.get("a".getBytes())));
        codeCache.put("b".getBytes(),"2".getBytes());
        codeCache.put("b".getBytes(),"3".getBytes());
        codeCache.put("c".getBytes(),"4".getBytes());
        /*************END code缓存*******************/

        accountStateCache.flush();
        Assert.assertNotNull(dbSource.get(key));
        //Assert.assertNull(dbSource.get("a".getBytes()));

        codeCache.flush();
        Assert.assertEquals(new String(dbSource.get("a".getBytes())),"1");
        Assert.assertEquals(new String(dbSource.get("b".getBytes())),"3");
        Assert.assertEquals(new String(dbSource.get("c".getBytes())),"4");
    }

    @Test
    public void testDelete() throws Exception {

    }
}