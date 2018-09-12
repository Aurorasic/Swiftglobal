package com.higgsblock.global.chain.vm.core;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.datasource.CachedSource;
import com.higgsblock.global.chain.vm.datasource.MultiCache;
import com.higgsblock.global.chain.vm.datasource.Source;
import com.higgsblock.global.chain.vm.datasource.WriteCache;
import com.higgsblock.global.chain.vm.util.ByteUtil;
import com.higgsblock.global.chain.vm.util.FastByteComparisons;
import com.higgsblock.global.chain.vm.util.HashUtil;
import com.higgsblock.global.chain.vm.util.NodeKeyCompositor;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author tangkun
 * @date 2018-09-06
 */
public class RepositoryImpl implements Repository {

    protected RepositoryImpl parent;

    //protected Source<byte[], AccountState> accountStateCache;
    //protected Source<byte[], byte[]> codeCache;

    private Map<byte[], AccountState> accountStateCache;

    private Map<byte[], byte[]> codeCache;

    protected MultiCache<? extends CachedSource<DataWord,DataWord>> storageCache;

    @Autowired
    protected SystemProperties config = SystemProperties.getDefault();

    public RepositoryImpl() {

        this.codeCache = new HashMap<>();
        this.accountStateCache = new HashMap<>();

    }

    public RepositoryImpl(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,
                          MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache) {
        init(accountStateCache, codeCache, storageCache);
    }

    protected void init(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,
                        MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache) {
        //this.accountStateCache = accountStateCache;
        //this.codeCache = codeCache;
        this.storageCache = storageCache;
    }

    @Override
    public synchronized AccountState createAccount(byte[] addr) {
        AccountState state = new AccountState(BigInteger.ZERO,addr);
        accountStateCache.put(addr, state);
        return state;
    }

    @Override
    public synchronized boolean isExist(byte[] addr) {
        return getAccountState(addr) != null;
    }

    @Override
    public synchronized AccountState getAccountState(byte[] addr) {
        System.out.println(Hex.toHexString(addr));
        accountStateCache.keySet().stream().forEach(item->System.out.println(Hex.toHexString(item)));

        return accountStateCache.get(addr);
    }

    synchronized AccountState getOrCreateAccountState(byte[] addr) {
        AccountState ret = accountStateCache.get(addr);
        if (ret == null) {
            ret = createAccount(addr);
        }
        return ret;
    }

    @Override
    public synchronized void delete(byte[] addr) {
        accountStateCache.remove(addr);
        //storageCache.delete(addr);
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return false;
    }


    @Override
    public synchronized void saveCode(byte[] addr, byte[] code) {
        byte[] codeHash = HashUtil.sha3(code);
        codeCache.put(codeKey(codeHash, addr), code);
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withCodeHash(codeHash));
    }

    @Override
    public synchronized byte[] getCode(byte[] addr) {
        byte[] codeHash = getCodeHash(addr);
        return FastByteComparisons.equal(codeHash, HashUtil.EMPTY_DATA_HASH) ?
                ByteUtil.EMPTY_BYTE_ARRAY : codeCache.get(codeKey(codeHash, addr));
    }



    @Override
    public byte[] getCodeHash(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState != null ? accountState.getCodeHash() : HashUtil.EMPTY_DATA_HASH;
    }

    @Override
    public synchronized void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        getOrCreateAccountState(addr);

        Source<DataWord, DataWord> contractStorage = storageCache.get(addr);
        contractStorage.put(key, value.isZero() ? null : value);
    }

    @Override
    public synchronized DataWord getStorageValue(byte[] addr, DataWord key) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? null : storageCache.get(addr).get(key);
    }

    @Override
    public synchronized BigInteger getBalance(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? BigInteger.ZERO : accountState.getBalance();
    }

    @Override
    public synchronized BigInteger addBalance(byte[] addr, BigInteger value) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withBalanceIncrement(value));
        return accountState.getBalance();
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return null;
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {

    }

//    @Override
//    public Repository startTracking() {
//        return null;
//    }

    @Override
    public void flush() {

    }

    @Override
    public void flushNoReconnect() {

    }

    @Override
    public synchronized RepositoryImpl startTracking() {
//        Source<byte[], AccountState> trackAccountStateCache = new WriteCache.BytesKey<>(accountStateCache,
//                WriteCache.CacheType.SIMPLE);
//        Source<byte[], byte[]> trackCodeCache = new WriteCache.BytesKey<>(codeCache, WriteCache.CacheType.SIMPLE);
//        MultiCache<CachedSource<DataWord, DataWord>> trackStorageCache = new MultiCache(storageCache) {
//            @Override
//            protected CachedSource create(byte[] key, CachedSource srcCache) {
//                return new WriteCache<>(srcCache, WriteCache.CacheType.SIMPLE);
//            }
//        };
//
//        RepositoryImpl ret = new RepositoryImpl(trackAccountStateCache, trackCodeCache, trackStorageCache);
//        ret.parent = this;
//        return ret;

        return null;
    }

    // composing a key as there can be several contracts with the same code
    private byte[] codeKey(byte[] codeHash, byte[] addr) {
        return NodeKeyCompositor.compose(codeHash, addr);
    }

    @Override
    public synchronized Repository getSnapshotTo(byte[] root) {
        return parent.getSnapshotTo(root);
    }

    @Override
    public synchronized void commit() {
        Repository parentSync = parent == null ? this : parent;
        // need to synchronize on parent since between different caches flush
        // the parent repo would not be in consistent state
        // when no parent just take this instance as a mock
        synchronized (parentSync) {
//            storageCache.flush();
////            codeCache.flush();
////            accountStateCache.flush();
        }
    }

    @Override
    public synchronized void rollback() {
        // nothing to do, will be GCed
    }

    @Override
    public void syncToRoot(byte[] root) {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public void reset() {

    }

    @Override
    public byte[] getRoot() {
        throw new RuntimeException("Not supported");
    }

    public synchronized String getTrieDump() {
        return dumpStateTrie();
    }

    public String dumpStateTrie() {
        throw new RuntimeException("Not supported");
    }

}
