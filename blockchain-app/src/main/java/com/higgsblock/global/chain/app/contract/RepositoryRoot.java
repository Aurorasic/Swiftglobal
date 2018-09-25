package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.core.AccountState;
import com.higgsblock.global.chain.vm.datasource.*;
import com.higgsblock.global.chain.vm.datasource.leveldb.LevelDbDataSource;
import com.higgsblock.global.chain.vm.util.HashUtil;
import com.higgsblock.global.chain.vm.util.NodeKeyCompositor;
import com.higgsblock.global.chain.vm.util.Serializer;
import com.higgsblock.global.chain.vm.util.Serializers;

public class RepositoryRoot extends RepositoryImpl {

    private Source<byte[], byte[]> sourceWriter;

    private Source<byte[], byte[]> storageCache;

    public RepositoryRoot() {
        //Source dbSource = new HashMapDB<byte[]>();
        DbSource<byte[]> dbSource = new LevelDbDataSource();
        dbSource.setName("contract");
        dbSource.init(DbSettings.DEFAULT);
        sourceWriter = new BatchSourceWriter<>(dbSource);

        //Source<byte[], byte[]> accounts = new XorDataSource<>(sourceWriter, HashUtil.sha3("account".getBytes()));

        //Source<byte[], byte[]> codes = new XorDataSource<>(sourceWriter, HashUtil.sha3("code".getBytes()));

        //Source<byte[], byte[]> storages = new XorDataSource<>(sourceWriter, HashUtil.sha3("storage".getBytes()));

        SourceCodec.BytesKey<AccountState, byte[]> accountStateCodec = new SourceCodec.BytesKey<>(sourceWriter, Serializers.AccountStateSerializer);
        ReadWriteCache.BytesKey<AccountState> accountStateCache = new ReadWriteCache.BytesKey(accountStateCodec, WriteCache.CacheType.SIMPLE);
        accountStateCache.setFlushSource(true);

        ReadWriteCache.BytesKey<byte[]> codeCache = new ReadWriteCache.BytesKey<>(sourceWriter, WriteCache.CacheType.SIMPLE);
        codeCache.setFlushSource(true);

        storageCache = new WriteCache.BytesKey<>(sourceWriter, WriteCache.CacheType.COUNTING);
        ((WriteCache.BytesKey<byte[]>) storageCache).setFlushSource(true);

        MultiCache<CachedSource<DataWord, DataWord>> tempStorageCache = new MultiCache(null) {
            @Override
            protected CachedSource create(byte[] key, CachedSource srcCache) {

                Serializer<byte[], byte[]> keyCompositor = new NodeKeyCompositor(key);
                Source<byte[], byte[]> composingSrc = new SourceCodec.KeyOnly<>(storageCache, keyCompositor);

                SourceCodec  sourceCodec = new SourceCodec<>(composingSrc, Serializers.StorageKeySerializer, Serializers.StorageValueSerializer);

                return new WriteCache<>(sourceCodec, WriteCache.CacheType.SIMPLE);
            }
        };

        init(accountStateCache, codeCache, tempStorageCache);
    }

    @Override
    public synchronized void commit() {
        super.commit();

        storageCache.flush();
        sourceWriter.flush();
    }

    @Override
    public void flush() {
        commit();
    }
}
