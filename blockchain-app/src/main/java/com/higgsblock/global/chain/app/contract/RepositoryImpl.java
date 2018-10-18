package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.datasource.*;
import com.higgsblock.global.chain.vm.util.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.*;


/**
 * @author tangkun
 * @date 2018-09-06
 */
@Setter
@Getter
@Slf4j
public class RepositoryImpl implements Repository {

    protected RepositoryImpl parent;

    private UTXOServiceProxy utxoServiceProxy;

    protected Source<byte[], AccountState> accountStateCache;

    protected Source<byte[], byte[]> codeCache;

    protected MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache;

    private SystemProperties config;

    List<AbstractCachedSource<byte[], ?>> writeCaches = new ArrayList<>();

    Map<byte[], AccountState> accountStates = new HashMap<>();

    List<AccountDetail> accountDetails = new ArrayList<>();

    Map<String, Set> unspentUTXOCache = new HashMap<>(16);

    Map<String, Set> spentUTXOCache = new HashMap<>(16);

    private String preBlockHash;

    protected RepositoryImpl() {
    }

    protected RepositoryImpl(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,

                             MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache, String preBlockHash) {
        this.preBlockHash = preBlockHash;
        init(accountStateCache, codeCache, storageCache);
    }

    protected void init(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,
                        MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache) {
        this.accountStateCache = accountStateCache;
        this.codeCache = codeCache;
        this.storageCache = storageCache;
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

    synchronized AccountState getOrCreateAccountState(byte[] addr) {
        AccountState ret = accountStateCache.get(addr);
        if (ret == null) {
            ret = createAccount(addr);
        }
        return ret;
    }

    @Override
    public synchronized long getNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? 0 : accountState.getNonce();
    }

    @Override
    public long increaseNonce(byte[] addr) {
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(addr, accountState.withIncrementedNonce());
        return accountState.getNonce();
    }

    @Override
    public synchronized void delete(byte[] addr) {
        AccountState ret = accountStateCache.get(addr);
        if (ret != null) {
            Set<ByteArrayWrapper> keys = ret.getKeys();
            Source<DataWord, DataWord> contractStorage = storageCache.get(addr);

            for (ByteArrayWrapper key : keys) {
                contractStorage.put(new DataWord(key), null);
            }

            byte[] codeHash = ret.getCodeHash();
            byte[] key = codeKey(codeHash, addr);
            codeCache.delete(key);
        }

        accountStateCache.delete(addr);
        //storageCache.delete(addr);
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return new ContractDetailsImpl(addr);
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
        AccountState accountState = getOrCreateAccountState(addr);

        accountStateCache.put(addr, accountState.withStorageKey(new ByteArrayWrapper(key.getData())));

        Source<DataWord, DataWord> contractStorage = storageCache.get(addr);
        contractStorage.put(key, value.isZero() ? null : value);
    }

    @Override
    public synchronized DataWord getStorageValue(byte[] addr, DataWord key) {
        AccountState accountState = getAccountState(addr);

        if (accountState == null) {
            return null;
        } else {
            return storageCache.get(addr).get(key);
        }
    }

    @Override
    public synchronized BigInteger getBalance(byte[] addr) {
        AccountState accountState = getAccountState(addr, null);
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

    /**
     * flush child cache to parent cache
     */
    @Override
    public void flush() {
        //sourceWriter.flush();
    }

    @Override
    public void flushNoReconnect() {

    }

    @Override
    public synchronized RepositoryImpl startTracking() {

        Source<byte[], AccountState> trackAccountStateCache = new WriteCache.BytesKey<>(accountStateCache, WriteCache.CacheType.SIMPLE);
        Source<byte[], byte[]> trackCodeCache = new WriteCache.BytesKey<>(codeCache, WriteCache.CacheType.SIMPLE);

        MultiCache<CachedSource<DataWord, DataWord>> trackStorageCache = new MultiCache(storageCache) {
            @Override
            protected CachedSource create(byte[] key, CachedSource srcCache) {
                return new WriteCache<>(srcCache, WriteCache.CacheType.SIMPLE);
            }
        };

        RepositoryImpl ret = new RepositoryImpl(trackAccountStateCache, trackCodeCache, trackStorageCache, this.preBlockHash);
        ret.setUtxoServiceProxy(this.utxoServiceProxy);
        ret.setConfig(this.config);
        ret.parent = this;
        return ret;
    }

    /**
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

            //flush UTXO into parent cache
            if (parent != null) {
                parent.mergeUTXO(this.spentUTXOCache, this.unspentUTXOCache);
                parent.accountDetails.addAll(this.accountDetails);
            }
            this.spentUTXOCache.clear();
            this.unspentUTXOCache.clear();

            // parent.accountStates.;
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


    //}

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
        AccountState contractAccount = this.getAccountState(from, currency);
        if (StringUtils.isEmpty(currency)) {
            currency = SystemCurrencyEnum.CAS.getCurrency();
        }
        if (contractAccount.getBalance().compareTo(amount) < 0) {
            LOGGER.warn("not enough balance");
            throw new RuntimeException("not enough balance ");
        }
        contractAccount.withBalanceDecrement(amount);
        AccountDetail accountDetail = new AccountDetail(from, address, amount, contractAccount.getBalance(), currency);
        accountDetails.add(accountDetail);
    }

    @Override
    public Set<UTXO> getUnSpendAsset(String address) {
        if (unspentUTXOCache.get(address) == null) {
            Set<UTXO> set = parent.getUnSpendAsset(address);
            unspentUTXOCache.put(address, set);
        }
        return parent.unspentUTXOCache.get(address);
    }

    /**
     * get spend asset
     *
     * @param address
     * @return
     */
    @Override
    public Set<UTXO> getSpendAsset(String address) {
        return spentUTXOCache.get(address);
    }

    /**
     * @param address
     * @param balance
     * @param currency
     * @return
     */
    @Override
    public AccountState createAccountState(byte[] address, BigInteger balance, String currency) {

        AccountState ret = accountStateCache.get(address);
        if (ret == null) {
            AccountState accountState = new AccountState(0, balance, address, currency, new HashSet<>());
            accountStateCache.put(address, accountState);
            return accountState;
        }

        return ret;
    }

    @Override
    public List<AccountDetail> getAccountDetails() {
        return accountDetails;
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

        for (Map.Entry<String, Set> account : spendUTXO.entrySet()) {
            unspentUTXOCache.get(account.getKey()).removeAll(account.getValue());
        }
        for (Map.Entry<String, Set> account : unSpendUTXO.entrySet()) {
            unspentUTXOCache.get(account.getKey()).addAll(account.getValue());
        }
        for (Map.Entry<String, Set> account : spendUTXO.entrySet()) {
            spentUTXOCache.get(account.getKey()).addAll(account.getValue());
        }
        return true;
    }

    @Override
    public boolean mergeUTXO2Parent(Map<String, Set> unSpendUTXO) {
        for (Map.Entry<String, Set> account : unSpendUTXO.entrySet()) {
            parent.unspentUTXOCache.getOrDefault(account.getKey(), new HashSet<>()).add(account.getValue());
        }
        return true;
    }

    /**
     * add utxo into first cache and build Account
     *
     * @return
     */
    @Override
    public boolean addUTXO(Object utxo) {

        // unspentUTXOCache.get(utxo.getAddress()).add(utxo);

        return true;
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

    /**
     * get account local cache if not find , and find in parent cache and put local cache
     *
     * @param address account address
     * @return
     */
    @Override
    public AccountState getAccountState(byte[] address, String currency) {

        if (StringUtils.isEmpty(currency)) {
            currency = SystemCurrencyEnum.CAS.getCurrency();
        }
        return accountStateCache.get(address);

//        List<UTXO> chainUTXO = utxoServiceProxy.getUnionUTXO(preBlockHash, AddrUtil.toTransactionAddr(address), currency);
//        if (chainUTXO != null && chainUTXO.size() != 0) {
//            unspentUTXOCache.addAll(chainUTXO);
//        }
//
//        for (UTXO utxo : unspentUTXOCache) {
//            if (utxo.getAddress().equals(AddrUtil.toTransactionAddr(address))) {
//                accountState.withBalanceIncrement(BalanceUtil.convertMoneyToGas(utxo.getOutput().getMoney()));
//            }
//        }
//        accountStates.put(address, accountState);
//        return accountState;
    }

    class ContractDetailsImpl implements ContractDetails {

        private byte[] address;

        public ContractDetailsImpl(byte[] address) {
            this.address = address;
        }

        @Override
        public void put(DataWord key, DataWord value) {
            RepositoryImpl.this.addStorageRow(address, key, value);
        }

        @Override
        public DataWord get(DataWord key) {
            return RepositoryImpl.this.getStorageValue(address, key);
        }

        @Override
        public byte[] getCode() {
            return RepositoryImpl.this.getCode(address);
        }

        @Override
        public void setCode(byte[] code) {
            RepositoryImpl.this.saveCode(address, code);
        }

        @Override
        public Map<DataWord, DataWord> getStorage() {
            Map<DataWord, DataWord> storage = new HashMap<>(16);
            storageCache.getModified().stream().forEach(item -> {
                DataWord key = new DataWord(item);
                storage.put(key, storageCache.get(address).get(key));
            });
            return storage;
        }
    }
}
