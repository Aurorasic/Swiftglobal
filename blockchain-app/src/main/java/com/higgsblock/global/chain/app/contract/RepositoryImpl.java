package com.higgsblock.global.chain.app.contract;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.datasource.CachedSource;
import com.higgsblock.global.chain.vm.datasource.MultiCache;
import com.higgsblock.global.chain.vm.datasource.Source;
import com.higgsblock.global.chain.vm.util.ByteUtil;
import com.higgsblock.global.chain.vm.util.FastByteComparisons;
import com.higgsblock.global.chain.vm.util.HashUtil;
import com.higgsblock.global.chain.vm.util.NodeKeyCompositor;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author tangkun
 * @date 2018-09-06
 */
public class RepositoryImpl implements Repository<UTXO> {

    protected RepositoryImpl parent;

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;



    //protected Source<byte[], AccountState> accountStateCache;
    //protected Source<byte[], byte[]> codeCache;

    private Map<String, AccountState> accountStateCache;

    private Map<String, byte[]> codeCache;

    //protected MultiCache<? extends CachedSource<DataWord,DataWord>> storageCache;

    private  Map<String, Map<String, DataWord>> storageCache;




    @Autowired
    protected SystemProperties config = SystemProperties.getDefault();

    Map<String,AccountState> accountStates = new HashMap<>();

    List<AccountDetail> accountDetails = new ArrayList<>();

    List<UTXO> unspentUTXOCache = new ArrayList<>();
    List<UTXO> spentUTXOCache = new ArrayList<>();

    public RepositoryImpl() {

        this.codeCache = new HashMap<>();
        this.accountStateCache = new HashMap<>();
        this.storageCache = new HashMap<>();
    }

    public RepositoryImpl(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,

                          MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache) {
        init(accountStateCache, codeCache, storageCache);
    }

    protected void init(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,
                        MultiCache<? extends CachedSource<DataWord, DataWord>> storageCache) {
        //this.accountStateCache = accountStateCache;
        //this.codeCache = codeCache;

        //this.storageCache = storageCache;

    }

    @Override
    public synchronized AccountState createAccount(byte[] addr) {
        AccountState state = new AccountState(BigInteger.ZERO,addr);
        accountStateCache.put(Hex.toHexString(addr), state);
        return state;
    }

    @Override
    public synchronized boolean isExist(byte[] addr) {
        return getAccountState(addr) != null;
    }

    @Override
    public synchronized AccountState getAccountState(byte[] addr) {
        System.out.println(Hex.toHexString(addr));
        accountStateCache.keySet().stream().forEach(item->System.out.println(item));

        return accountStateCache.get(Hex.toHexString(addr));
    }

    synchronized AccountState getOrCreateAccountState(byte[] addr) {
        AccountState ret = accountStateCache.get((Hex.toHexString(addr)));
        if (ret == null) {
            ret = createAccount(addr);
        }
        return ret;
    }

    @Override
    public synchronized void delete(byte[] addr) {
        accountStateCache.remove((Hex.toHexString(addr)));
        storageCache.remove(Hex.toHexString(addr));
    }

    @Override
    public Map<String, DataWord> getContractDetails(byte[] addr) {
        return storageCache.get(Hex.toHexString(addr));
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return false;
    }


    @Override
    public synchronized void saveCode(byte[] addr, byte[] code) {
        byte[] codeHash = HashUtil.sha3(code);
        byte[] key =codeKey(codeHash, addr);
        String strKey =  Hex.toHexString(key);
        codeCache.put(strKey, code);
        AccountState accountState = getOrCreateAccountState(addr);
        accountStateCache.put(Hex.toHexString(addr), accountState.withCodeHash(codeHash));
    }

    @Override
    public synchronized byte[] getCode(byte[] addr) {
        byte[] codeHash = getCodeHash(addr);
        byte[] key = codeKey(codeHash, addr);
        String strKey =  Hex.toHexString(key);
        return FastByteComparisons.equal(codeHash, HashUtil.EMPTY_DATA_HASH) ?
                ByteUtil.EMPTY_BYTE_ARRAY : codeCache.get(strKey);
    }



    @Override
    public byte[] getCodeHash(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState != null ? accountState.getCodeHash() : HashUtil.EMPTY_DATA_HASH;
    }

    @Override
    public synchronized void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        getOrCreateAccountState(addr);

        String strKey = Hex.toHexString(addr);
        Map<String, DataWord> contractStorage = storageCache.get(addr);
        if (contractStorage == null) {
            contractStorage = new HashMap<>();
            storageCache.put(strKey, contractStorage);
        }

        String strSubKey = Hex.toHexString(key.getData());
        contractStorage.put(strSubKey, value.isZero() ? null : value);

        //Source<DataWord, DataWord> contractStorage = storageCache.get(addr);
        //contractStorage.put(key, value.isZero() ? null : value);
    }

    @Override
    public synchronized DataWord getStorageValue(byte[] addr, DataWord key) {
        AccountState accountState = getAccountState(addr);

        if (accountState == null) {
            return  null;
        } else {
            String strKey = Hex.toHexString(addr);
            Map<String, DataWord> contractStorage = storageCache.get(strKey);
            String strSubKey = Hex.toHexString(key.getData());

            if (contractStorage == null) {
                contractStorage = new HashMap<>();
                storageCache.put(strKey, contractStorage);
                return null;
            }

            return  contractStorage.get(strSubKey);
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
        accountStateCache.put(Hex.toHexString(addr), accountState.withBalanceIncrement(value));
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

    /**
     * flush child cache to parent cache
     * 1: UTXO
     * 2: storageCache
     * 3:codeCache
     */
    @Override
    public void flush() {

        //flush UTXO
        parent.mergeUTXO(this.spentUTXOCache,this.unspentUTXOCache);

        //flush storage
        parent.storageCache.putAll(this.storageCache);

        //flush codeCache
        parent.codeCache.putAll(this.codeCache);
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
    public void transfer(String from, String address, String amount, String currency) {
        AccountState to = this.getAccountState(from,address) ;
        if(to.getBalance().compareTo(new BigInteger(amount)) < 0 ){
            //余额不足
        }
        to.withBalanceDecrement(new BigInteger(amount));
        AccountDetail accountDetail = new AccountDetail(from,new String(to.getCodeHash()),
                new BigInteger(amount),to.getBalance(),currency);
        accountDetails.add(accountDetail);
    }

    @Override
    public List<UTXO> getUnSpendAsset(String address) {
        return unspentUTXOCache.stream().filter(item -> item.getAddress().equals(address)).collect(Collectors.toList());
    }

    /**
     * get spend asset
     *
     * @param address
     * @return
     */
    @Override
    public List<UTXO> getSpendAsset(String address) {
        return spentUTXOCache.stream().filter(item -> item.getAddress().equals(address)).collect(Collectors.toList());
    }

    /**
     * @param address
     * @param balance
     * @param currency
     * @return
     */
    @Override
    public AccountState createAccountState(String address, BigInteger balance, String currency) {

        AccountState accountState = new AccountState(balance,address.getBytes(), currency);
        accountStateCache.put(address,accountState);

        return accountState;
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
    public boolean mergeUTXO(List<UTXO> spendUTXO, List<UTXO> unSpendUTXO) {

        unSpendUTXO.removeAll(spendUTXO);
        unspentUTXOCache.addAll(unSpendUTXO);
        spentUTXOCache.addAll(spendUTXO);

        //更新utxo时，刷新账户考虑是否合约账户才需要做该操作
        for(UTXO utxo:spendUTXO){
           AccountState accountState =  accountStates.get(utxo.getAddress());
            accountState.withBalanceDecrement(BalanceUtil.convertMoneyToGas(utxo.getOutput().getMoney()));
        }

        for(UTXO utxo:unSpendUTXO){
            AccountState accountState =  accountStates.get(utxo.getAddress());
            if(accountState != null) {
                accountState.withBalanceIncrement(BalanceUtil.convertMoneyToGas(utxo.getOutput().getMoney()));
            }
        }
        return true;
    }

    /**
     * add utxo into first cache and build Account
     *
     * @return
     */
    @Override
    public boolean addUTXOAndBuildAccount(String address,UTXO utxo,String currency) {

        AccountState accountState = accountStates.get(address);
        if(accountState == null){
            accountState = createAccountState(address, BigInteger.ZERO, currency);
            List<UTXO> chainUTXO = utxoServiceProxy.getUnionUTXO("preBlockHash",address,currency);
            if(chainUTXO != null && chainUTXO.size() != 0) {
                unspentUTXOCache.addAll(chainUTXO);
                accountState.withBalanceIncrement(Helpers.convertBalance(chainUTXO));
            }
        }
        unspentUTXOCache.add(utxo);
        accountState.withBalanceIncrement(BalanceUtil.convertMoneyToGas(utxo.getOutput().getMoney()));

        accountStates.put(address,accountState);
        return true;
    }

    /**
     * get account local cache if not find , and find in parent cache and put local cache
     *
     * @param address account address
     * @return
     */
    @Override
    public AccountState getAccountState(String address,String currency) {

        AccountState accountState = accountStateCache.get(address);
        if(accountState != null){
            return  accountState;
        }

        if(accountState == null && parent != null){
            accountState = parent.getAccountState(address,currency);
            accountStates.put(address,accountState);
            return  accountState;
        }

        // first cache
        accountState = createAccountState(address, BigInteger.ZERO, currency);
        List<UTXO> chainUTXO = utxoServiceProxy.getUnionUTXO("preBlockHash",address,currency);
        if(chainUTXO != null && chainUTXO.size() != 0) {
            unspentUTXOCache.addAll(chainUTXO);
        }

        for(UTXO utxo:unspentUTXOCache){
            if(utxo.getAddress().equals(address)){
                accountState.withBalanceIncrement(BalanceUtil.convertMoneyToGas(utxo.getOutput().getMoney()));
            }
        }
        accountStates.put(address,accountState);
        return accountState;
    }
}
