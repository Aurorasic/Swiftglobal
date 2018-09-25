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

    public RepositoryRoot() {
        //Source dbSource = new HashMapDB<byte[]>();
        DbSource<byte[]> dbSource = new LevelDbDataSource();
        dbSource.setName("contract");
        dbSource.init(DbSettings.DEFAULT);
        sourceWriter = new BatchSourceWriter<>(dbSource);

        Source<byte[], byte[]> accounts = new XorDataSource<>(sourceWriter, HashUtil.sha3("account".getBytes()));

        Source<byte[], byte[]> codes = new XorDataSource<>(sourceWriter, HashUtil.sha3("code".getBytes()));

        Source<byte[], byte[]> storages = new XorDataSource<>(sourceWriter, HashUtil.sha3("storage".getBytes()));

        SourceCodec.BytesKey<AccountState, byte[]> accountStateCodec = new SourceCodec.BytesKey<>(accounts, Serializers.AccountStateSerializer);
        ReadWriteCache.BytesKey<AccountState> accountStateCache = new ReadWriteCache.BytesKey(accountStateCodec, WriteCache.CacheType.SIMPLE);
        accountStateCache.setFlushSource(true);

        ReadWriteCache.BytesKey<byte[]> codeCache = new ReadWriteCache.BytesKey<>(codes, WriteCache.CacheType.SIMPLE);
        codeCache.setFlushSource(true);

        MultiCache<CachedSource<DataWord, DataWord>> storageCache = new MultiCache(storages) {
            @Override
            protected CachedSource create(byte[] key, CachedSource srcCache) {

                Serializer<byte[], byte[]> keyCompositor = new NodeKeyCompositor(key);
                Source<byte[], byte[]> composingSrc = new SourceCodec.KeyOnly<>(storages, keyCompositor);

                SourceCodec  sourceCodec = new SourceCodec<>(composingSrc, Serializers.StorageKeySerializer, Serializers.StorageValueSerializer);

                return new ReadWriteCache<>(sourceCodec, WriteCache.CacheType.SIMPLE);
            }
        };
        storageCache.setFlushSource(true);

        init(accountStateCache, codeCache, storageCache);
    }

    @Override
    public synchronized void commit() {
        super.commit();

        if (sourceWriter != null) {
            sourceWriter.flush();
        }
    }

    @Override
    public void flush() {
        commit();
    }
}
