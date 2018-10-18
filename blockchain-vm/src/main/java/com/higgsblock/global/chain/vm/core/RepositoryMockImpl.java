package com.higgsblock.global.chain.vm.core;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.datasource.*;
import com.higgsblock.global.chain.vm.util.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.*;

/**
 * @author zhao xiaogang
 * @date 2018-09-06
 */
public class RepositoryMockImpl implements Repository {

    protected RepositoryMockImpl parent;


    private Source<byte[], AccountState> accountStateCache;
    private Source<byte[], byte[]> codeCache;
    private MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache;
    private Map<ByteArrayWrapper, Set<ByteArrayWrapper>> addrKeys = new HashMap<>();

    /**
     * utxo cache
     */
    protected Map<String, List<UTXOBO>> utxoCache;


    @Autowired
    protected SystemProperties config = SystemProperties.getDefault();

    public RepositoryMockImpl() {
        Source dbSource = new HashMapDB<byte[]>();
        Source<byte[], AccountState> accountStateCache = new WriteCache.BytesKey<>(dbSource,
                WriteCache.CacheType.SIMPLE);
        Source<byte[], byte[]> codeCache = new WriteCache.BytesKey<>(dbSource, WriteCache.CacheType.SIMPLE);
        MultiCache<CachedSource<DataWord, DataWord>> storageCache = new MultiCache(dbSource) {
            @Override
            protected CachedSource create(byte[] key, CachedSource srcCache) {
                return new WriteCache<>(srcCache, WriteCache.CacheType.SIMPLE);
            }
        };

        init(accountStateCache, codeCache, storageCache);
    }

    @Override
    public long getNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? 0 : accountState.getNonce();
    }

    @Override
    public long increaseNonce(byte[] addr) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withIncrementedNonce());
        return accountState.getNonce();
    }

    public RepositoryMockImpl(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,

                              MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache) {
        init(accountStateCache, codeCache, storageCache);
    }

    private void init(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,
                      MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache) {
        this.accountStateCache = accountStateCache;
        this.codeCache = codeCache;

        this.storageCache = storageCache;
        this.utxoCache = utxoCache;
    }

    @Override
    public synchronized AccountState createAccount(byte[] addr) {
        AccountState state = new AccountState(0, BigInteger.ZERO);
        accountStateCache.put(addr, state);
        return state;
    }

    @Override
    public synchronized boolean isExist(byte[] addr) {
        return getAccountState(addr) != null;
    }

    @Override
    public synchronized AccountState getAccountState(byte[] addr) {
        return accountStateCache.get(addr);
    }

    /**
     * get account local cache if not find , and find in parent cache and put local cache
     *
     * @param address  account address
     * @param currency
     * @return
     */
    @Override
    public AccountState getAccountState(byte[] address, String currency) {
        return null;
    }

    synchronized AccountState getOrCreateAccountState(byte[] addr) {
        AccountState ret = accountStateCache.get((addr));
        if (ret == null) {
            ret = createAccount(addr);
        }
        return ret;
    }

    @Override
    public synchronized void delete(byte[] addr) {
        accountStateCache.delete(addr);
        storageCache.delete(addr);
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return new ContractDetailsMockImpl(addr);
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return false;
    }


    @Override
    public synchronized void saveCode(byte[] addr, byte[] code) {
        byte[] codeHash = HashUtil.sha3(code);
        byte[] key = codeKey(codeHash, addr);
        codeCache.put(key, code);
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withCodeHash(codeHash));
    }

    @Override
    public synchronized byte[] getCode(byte[] addr) {
        byte[] codeHash = getCodeHash(addr);
        byte[] key = codeKey(codeHash, addr);
        return FastByteComparisons.equal(codeHash, HashUtil.EMPTY_DATA_HASH) ?
                ByteUtil.EMPTY_BYTE_ARRAY : codeCache.get(key);
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

        Set<ByteArrayWrapper> keys = addrKeys.get(addr);
        if (keys == null) {
            addrKeys.put(new ByteArrayWrapper(addr), new HashSet<>());
        } else {
            keys.add(new ByteArrayWrapper(value.isZero() ? null : value.getData()));
        }
    }

    @Override
    public synchronized DataWord getStorageValue(byte[] addr, DataWord key) {
        AccountState accountState = getAccountState(addr);

        if (accountState == null) {
            return null;
        } else {
            Source<DataWord, DataWord> contractStorage = storageCache.get(addr);
            return contractStorage.get(key);
        }
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

    @Override
    public void flush() {

    }

    @Override
    public void flushNoReconnect() {

    }

    @Override
    public synchronized RepositoryMockImpl startTracking() {
        Source<byte[], AccountState> trackAccountStateCache = new WriteCache.BytesKey<>(accountStateCache,
                WriteCache.CacheType.SIMPLE);
        Source<byte[], byte[]> trackCodeCache = new WriteCache.BytesKey<>(codeCache, WriteCache.CacheType.SIMPLE);
        MultiCache<CachedSource<DataWord, DataWord>> trackStorageCache = new MultiCache(storageCache) {
            @Override
            protected CachedSource create(byte[] key, CachedSource srcCache) {
                return new WriteCache<>(srcCache, WriteCache.CacheType.SIMPLE);
            }
        };

        RepositoryMockImpl ret = new RepositoryMockImpl(trackAccountStateCache, trackCodeCache, trackStorageCache);
        ret.parent = this;
        return ret;
    }

    /**
     * composing a key as there can be several contracts with the same code
     * /
     *
     * @param codeHash
     * @param addr
     * @return
     */
    private byte[] codeKey(byte[] codeHash, byte[] addr) {
        return NodeKeyCompositor.compose(codeHash, addr);
    }

    @Override
    public synchronized Repository getSnapshotTo(byte[] root) {
        return parent.getSnapshotTo(root);
    }


    @Override
    public String getBlockHashByNumber(long blockNumber, String branchBlockHash) {
        return null;
    }

    /**
     * transfer assert from to address
     *
     * @param from     balance must glt amount
     * @param address  receive address
     * @param amount   transfer amount
     * @param currency assert type
     */
    @Override
    public void transfer(byte[] from, byte[] address, BigInteger amount, String currency) {

    }

    /**
     * get unSpend asset
     *
     * @param address
     * @return
     */
    @Override
    public Set getUnSpendAsset(String address) {
        return null;
    }

    /**
     * get spend asset
     *
     * @param address
     * @return
     */
    @Override
    public Set getSpendAsset(String address) {
        return null;
    }

    /**
     * merge utxo
     *
     * @param spendUTXO
     * @param unSpendUTXO
     * @return
     */
    @Override
    public boolean mergeUTXO(Map<String, Set> spendUTXO, Map<String, Set> unSpendUTXO) {
        return false;
    }

    @Override
    public boolean mergeUTXO2Parent(Map<String, Set> unSpendUTXO) {
        return false;
    }

    /**
     * @param address
     * @param balance
     * @param currency
     * @return
     */
    @Override
    public AccountState createAccountState(byte[] address, BigInteger balance, String currency) {
        return null;
    }

    @Override
    public List<AccountDetail> getAccountDetails() {
        return null;
    }

    /**
     * add utxo into first cache and build Account
     *
     * @param o
     * @return
     */
    @Override
    public boolean addUTXO(Object o) {
        return false;
    }

    /**
     * get hash
     *
     * @return hash
     */
    @Override
    public String getHash() {
        return null;
    }

    @Override
    public synchronized void commit() {
        Repository parentSync = parent == null ? this : parent;
        // need to synchronize on parent since between different caches flush
        // the parent repo would not be in consistent state
        // when no parent just take this instance as a mock
        synchronized (parentSync) {
            storageCache.flush();
            codeCache.flush();
            accountStateCache.flush();

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


    public boolean flushImpl(RepositoryMockImpl childRepository) {

        //if parent utxo include child utxo and child's utxo is spent
        Map<String, List<UTXOBO>> utxoCache = childRepository.getUtxoCache();
        for (Map.Entry<String, List<UTXOBO>> en : utxoCache.entrySet()) {
            List<UTXOBO> cList = en.getValue();
            List<UTXOBO> pList = this.utxoCache.get(en.getKey());

        }

        return false;
    }

    public Map<String, List<UTXOBO>> getUtxoCache() {
        return utxoCache;
    }

    public void setUtxoCache(Map<String, List<UTXOBO>> utxoCache) {
        this.utxoCache = utxoCache;
    }


    class ContractDetailsMockImpl implements ContractDetails {

        private byte[] address;

        public ContractDetailsMockImpl(byte[] address) {
            this.address = address;
        }

        @Override
        public void put(DataWord key, DataWord value) {

        }

        @Override
        public DataWord get(DataWord key) {
            return null;
        }

        @Override
        public byte[] getCode() {
            return new byte[0];
        }

        @Override
        public void setCode(byte[] code) {

        }

        @Override
        public Map<DataWord, DataWord> getStorage() {
            Map<DataWord, DataWord> storage = new HashMap<>();

            addrKeys.get(address).stream().forEach(item -> {
                DataWord key = new DataWord(item.getData());
                DataWord value = storageCache.get(address).get(key);
                storage.put(key, value);
            });

            return storage;
        }
    }
}
