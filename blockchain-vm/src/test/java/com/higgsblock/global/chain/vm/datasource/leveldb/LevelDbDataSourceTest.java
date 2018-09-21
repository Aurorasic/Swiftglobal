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
       // writer.setFlushSource(true);

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
        //((WriteCache)conAccountStateCache).setFlushSource(true);
        byte[] key = "f".getBytes();
        AccountState accountState = new AccountState(0, BigInteger.valueOf(10));
        conAccountStateCache.put(key,accountState);

        /**********END 账户缓存******************/

        /*************START code缓存*******************/
        //block
        Source<byte[], byte[]> blCodeCache = new WriteCache.BytesKey<>(writer, WriteCache.CacheType.COUNTING);

        //tx
        Source<byte[], byte[]> txCodeCache = new WriteCache.BytesKey<>(blCodeCache, WriteCache.CacheType.COUNTING);


        //con
        Source<byte[], byte[]> conCodeCache = new WriteCache.BytesKey<>(txCodeCache, WriteCache.CacheType.COUNTING);
        conCodeCache.put(key,"3".getBytes());
        /*************END code缓存*******************/

       // accountStateCache.flush();
        //Assert.assertNotNull(dbSource.get(key));
        //Assert.assertNull(dbSource.get("a".getBytes()));

        //合约cache只提交到交易
        conCodeCache.flush();
        Assert.assertEquals(new String(txCodeCache.get(key)),"3");
        Assert.assertNull(blCodeCache.get(key));
        //交易的
        txCodeCache.flush();
        //区块的
        blCodeCache.flush();
       // Assert.assertEquals(new String(writer.get("e".getBytes())),"3");
        Assert.assertNull(dbSource.get(key));
        //统一提交
        writer.flush();

        Assert.assertEquals(new String(dbSource.get(key)),"3");
//        Assert.assertEquals(new String(dbSource.get("b".getBytes())),"3");
//        Assert.assertEquals(new String(dbSource.get("c".getBytes())),"4");
    }

    @Test
    public void testDelete() throws Exception {

    }
}