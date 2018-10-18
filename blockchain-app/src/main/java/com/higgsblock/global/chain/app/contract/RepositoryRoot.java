package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.IContractRepository;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.core.AccountState;
import com.higgsblock.global.chain.vm.core.SystemProperties;
import com.higgsblock.global.chain.vm.datasource.*;
import com.higgsblock.global.chain.vm.util.HashUtil;
import com.higgsblock.global.chain.vm.util.NodeKeyCompositor;
import com.higgsblock.global.chain.vm.util.Serializer;
import com.higgsblock.global.chain.vm.util.Serializers;
import lombok.Setter;

import java.util.*;

/**
 * @author zhao xiaogang
 * @date 2018-09-20
 */

@Setter
public class RepositoryRoot extends RepositoryImpl {

    private Source<byte[], byte[]> sourceWriter;

    private Source<byte[], byte[]> storageCache;

    private DbSource<byte[]> dbSource;

    private Map<String, Boolean> loadDBRecodes = new HashMap<>();


    /**
     * last time used RepositoryRoot；
     */
    private static RepositoryRoot lastRepositoryRoot;


    public RepositoryRoot(IContractRepository repository, String preBlockHash, UTXOServiceProxy utxoServiceProxy, SystemProperties config) {
        super.setPreBlockHash(preBlockHash);
        super.setUtxoServiceProxy(utxoServiceProxy);
        super.setConfig(config);

        this.dbSource = new ContractDataSource(repository);
        this.sourceWriter = new BatchSourceWriter<>(dbSource);

        Source<byte[], byte[]> xorAccountState = new XorDataSource<>(sourceWriter, HashUtil.sha3("account".getBytes()));
        Source<byte[], byte[]> xorCode = new XorDataSource<>(sourceWriter, HashUtil.sha3("code".getBytes()));
        Source<byte[], byte[]> xorStorage = new XorDataSource<>(sourceWriter, HashUtil.sha3("storage".getBytes()));

        SourceCodec.BytesKey<AccountState, byte[]> accountStateCodec = new SourceCodec.BytesKey<>(xorAccountState, Serializers.AccountStateSerializer);
        ReadWriteCache.BytesKey<AccountState> accountStateCache = new ReadWriteCache.BytesKey(accountStateCodec, WriteCache.CacheType.SIMPLE);
        accountStateCache.setFlushSource(true);

        ReadWriteCache.BytesKey<byte[]> codeCache = new ReadWriteCache.BytesKey<>(xorCode, WriteCache.CacheType.SIMPLE);
        codeCache.setFlushSource(true);

        storageCache = new WriteCache.BytesKey<>(xorStorage, WriteCache.CacheType.COUNTING);
        MultiCache<CachedSource<DataWord, DataWord>> tempStorageCache = new MultiCache(null) {
            @Override
            protected CachedSource create(byte[] key, CachedSource srcCache) {

                Serializer<byte[], byte[]> keyCompositor = new NodeKeyCompositor(key);
                Source<byte[], byte[]> composingSrc = new SourceCodec.KeyOnly<>(storageCache, keyCompositor);
                SourceCodec sourceCodec = new SourceCodec<>(composingSrc, Serializers.StorageKeySerializer, Serializers.StorageValueSerializer);

                return new WriteCache<>(sourceCodec, WriteCache.CacheType.SIMPLE);
            }
        };

        init(accountStateCache, codeCache, tempStorageCache);

        lastRepositoryRoot = this;
    }

    @Override
    public synchronized void commit() {
        super.commit();

        storageCache.flush();
        sourceWriter.flush();
        lastRepositoryRoot = null;
    }

    @Override
    public void flush() {
        commit();
    }


    public String getStateHash() {
        super.commit();
        storageCache.flush();
        return sourceWriter.getStateHash();
    }

    public static RepositoryRoot getLastRepositoryRoot() {
        return lastRepositoryRoot;
    }

    @Override
    public Set getUnSpendAsset(String address) {


        if (unspentUTXOCache == null) {
            unspentUTXOCache = new HashMap<>(16);
        }
        boolean cacheIsNullAndNotLoadedDd = unspentUTXOCache.get(address) == null
                && loadDBRecodes.get(address) == null;
        boolean notLoadedDd = loadDBRecodes.get(address) == null;

        if (cacheIsNullAndNotLoadedDd || notLoadedDd) {
            List<UTXO> chainUTXO = getUtxoServiceProxy().getUnionUTXO(getPreBlockHash(), address
                    , SystemCurrencyEnum.CAS.getCurrency());
            loadDBRecodes.put(address, true);
            if (chainUTXO != null && chainUTXO.size() != 0) {
                Set set = unspentUTXOCache.getOrDefault(address, new HashSet<>());
                set.addAll(chainUTXO);
                unspentUTXOCache.put(address, set);
            }
        }

        return unspentUTXOCache.get(address);
    }


}
