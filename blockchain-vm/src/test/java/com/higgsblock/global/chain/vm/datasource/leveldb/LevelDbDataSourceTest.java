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
        //二级缓存
        WriteCache.BytesKey<AccountState> ret = new WriteCache.BytesKey<>(accountStateCodec, WriteCache.CacheType.SIMPLE);
        ret.setFlushSource(true);

        //三级缓存
        Source<byte[], AccountState> accountStateCache = new WriteCache.BytesKey(ret,
                WriteCache.CacheType.SIMPLE);
        ((WriteCache)accountStateCache).setFlushSource(true);
        byte[] key = "hello1".getBytes();
      //  byte[] value = "world1".getBytes();
        AccountState accountState = new AccountState(0, BigInteger.valueOf(10));
        accountStateCache.put(key,accountState);
        accountStateCache.flush();
        Assert.assertNotNull(dbSource.get(key));
        System.out.println(accountStateCache.get("hello1".getBytes()).getBalance());


    }

    @Test
    public void testDelete() throws Exception {

    }
}