package com.higgsblock.global.chain.app.contract;

/**
 * @author Chen Jiawei
 * @date 2018-09-29
 */
//public class RepositoryImplTest implements Repository {
//    private Map<byte[], byte[]> db = new HashMap<byte[], byte[]>();
//
//    @Override
//    public synchronized long getNonce(byte[] addr) {
//        return 0;
//    }
//
//    @Override
//    public long increaseNonce(byte[] addr) {
//        return 0;
//    }
//
//    @Override
//    public AccountState createAccount(byte[] addr) {
//        db.put(addr, new byte[0]);
//        System.out.println(db.get(addr) != null);
//        return null;
//    }
//
//    @Override
//    public boolean isExist(byte[] addr) {
//        return db.get(addr) != null;
//    }
//
//    @Override
//    public AccountState getAccountState(byte[] addr) {
//        return null;
//    }
//
//    @Override
//    public AccountState getAccountState(byte[] address, String currency) {
//        return null;
//    }
//
//    @Override
//    public void delete(byte[] addr) {
//
//    }
//
//    @Override
//    public ContractDetails getContractDetails(byte[] addr) {
//        return null;
//    }
//
//    @Override
//    public boolean hasContractDetails(byte[] addr) {
//        return false;
//    }
//
//    @Override
//    public void saveCode(byte[] addr, byte[] code) {
//
//    }
//
//    @Override
//    public byte[] getCode(byte[] addr) {
//        return new byte[0];
//    }
//
//    @Override
//    public byte[] getCodeHash(byte[] addr) {
//        return new byte[0];
//    }
//
//    @Override
//    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
//
//    }
//
//    @Override
//    public DataWord getStorageValue(byte[] addr, DataWord key) {
//        return null;
//    }
//
//    @Override
//    public BigInteger getBalance(byte[] addr) {
//        return null;
//    }
//
//    @Override
//    public BigInteger addBalance(byte[] addr, BigInteger value) {
//        return null;
//    }
//
//    @Override
//    public Set<byte[]> getAccountsKeys() {
//        return null;
//    }
//
//    @Override
//    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
//
//    }
//
//    @Override
//    public Repository startTracking() {
//        return new RepositoryMockImpl();
//    }
//
//    @Override
//    public void flush() {
//
//    }
//
//    @Override
//    public void flushNoReconnect() {
//
//    }
//
//    @Override
//    public void commit() {
//
//    }
//
//    @Override
//    public void rollback() {
//
//    }
//
//    @Override
//    public void syncToRoot(byte[] root) {
//
//    }
//
//    @Override
//    public boolean isClosed() {
//        return false;
//    }
//
//    @Override
//    public void close() {
//
//    }
//
//    @Override
//    public void reset() {
//
//    }
//
//    @Override
//    public byte[] getRoot() {
//        return new byte[0];
//    }
//
//    @Override
//    public Repository getSnapshotTo(byte[] root) {
//        return null;
//    }
//
//    @Override
//    public String getBlockHashByNumber(long blockNumber, String branchBlockHash) {
//        return null;
//    }
//
//    @Override
//    public void transfer(byte[] from, byte[] address, BigInteger amount, String currency) {
//
//    }
//
//    @Override
//    public List getUnSpendAsset(byte[] address) {
//        return null;
//    }
//
//    @Override
//    public List getSpendAsset(byte[] address) {
//        return null;
//    }
//
//    @Override
//    public boolean mergeUTXO(List spendUTXO, List unSpendUTXO) {
//        return false;
//    }
//
//    @Override
//    public AccountState createAccountState(byte[] address, BigInteger balance, String currency) {
//        return null;
//    }
//
//    @Override
//    public List<AccountDetail> getAccountDetails() {
//        return null;
//    }
//
//    @Override
//    public boolean addUTXO(Object o) {
//        return false;
//    }
//
//    @Override
//    public String getHash() {
//        return null;
//    }
//}
