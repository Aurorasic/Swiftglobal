package com.higgsblock.global.chain.vm.core;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.datasource.Source;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tangkun
 * @date 2018-09-06
 */
public interface Repository<ASSET> {
    /**
     * Create a new account in the database
     *
     * @param addr of the contract
     * @return newly created account state
     */
    AccountState createAccount(byte[] addr);


    /**
     * @param addr - account to check
     * @return - true if account exist,
     *           false otherwise
     */
    boolean isExist(byte[] addr);

    /**
     * Retrieve an account
     *
     * @param addr of the account
     * @return account state as stored in the database
     */
    AccountState getAccountState(byte[] addr);

    /**
     * get account local cache if not find , and find in parent cache and put local cache
     * @param address account address
     * @return
     */
    AccountState getAccountState(String addr,String currency);

    /**
     * Deletes the account
     *
     * @param addr of the account
     */
    void delete(byte[] addr);

    /**
     * Get current nonce of a given account
     *
     * @param addr of the account
     * @return value of the nonce
     */
    BigInteger getNonce(byte[] addr);

    /**
     * Retrieve contract details for a given account from the database
     *
     * @param addr of the account
     * @return new contract details
     */
    ContractDetails getContractDetails(byte[] addr);



    boolean hasContractDetails(byte[] addr);

    /**
     * Store code associated with an account
     *
     * @param addr for the account
     * @param code that will be associated with this account
     */
    void saveCode(byte[] addr, byte[] code);

    /**
     * Retrieve the code associated with an account
     *
     * @param addr of the account
     * @return code in byte-array format
     */
    byte[] getCode(byte[] addr);

    /**
     * Retrieve the code hash associated with an account
     *
     * @param addr of the account
     * @return code hash
     */
    byte[] getCodeHash(byte[] addr);

    /**
     * Put a value in storage of an account at a given key
     *
     * @param addr of the account
     * @param key of the data to store
     * @param value is the data to store
     */
    void addStorageRow(byte[] addr, DataWord key, DataWord value);


    /**
     * Retrieve storage value from an account for a given key
     *
     * @param addr of the account
     * @param key associated with this value
     * @return data in the form of a <code>DataWord</code>
     */
    DataWord getStorageValue(byte[] addr, DataWord key);


    /**
     * Retrieve balance of an account
     *
     * @param addr of the account
     * @return balance of the account as a <code>BigInteger</code> value
     */
    BigInteger getBalance(byte[] addr);

    /**
     * Add value to the balance of an account
     *
     * @param addr of the account
     * @param value to be added
     * @return new balance of the account
     */
    BigInteger addBalance(byte[] addr, BigInteger value);

    /**
     * @return Returns set of all the account addresses
     */
    Set<byte[]> getAccountsKeys();


    /**
     * Dump the full state of the current repository into a file with JSON format
     * It contains all the contracts/account, their attributes and
     *
     * @param block of the current state
     * @param gasUsed the amount of gas used in the block until that point
     * @param txNumber is the number of the transaction for which the dump has to be made
     * @param txHash is the hash of the given transaction.
     * If null, the block state post coinbase reward is dumped.
     */
    void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash);

    /**
     * Save a snapshot and start tracking future changes
     *
     * @return the tracker repository
     */
    Repository startTracking();

    /**
     * flush child cache to parent cache
     */
    void flush();

    void flushNoReconnect();


    /**
     * Store all the temporary changes made
     * to the repository in the actual database
     */
    void commit();

    /**
     * Undo all the changes made so far
     * to a snapshot of the repository
     */
    void rollback();

    /**
     * Return to one of the previous snapshots
     * by moving the root.
     *
     * @param root - new root
     */
    void syncToRoot(byte[] root);

    /**
     * Check to see if the current repository has an open connection to the database
     *
     * @return <tt>true</tt> if connection to database is open
     */
    boolean isClosed();

    /**
     * Close the database
     */
    void close();

    /**
     * Reset
     */
    void reset();




    byte[] getRoot();



    Repository getSnapshotTo(byte[] root);


    String getBlockHashByNumber(long blockNumber, String branchBlockHash);

    /**
     * transfer assert from to address
     * @param from balance must glt amount
     * @param address  receive address
     * @param amount transfer amount
     * @param currency assert type
     */
    void transfer(String from,String address ,String amount,String currency);

    /**
     * get unSpend asset
     * @param address
     * @return
     */
    List<ASSET> getUnSpendAsset(String address);

    /**
     * get spend asset
     * @param address
     * @return
     */
    List<ASSET> getSpendAsset(String address);

    /**
     * merge utxo
     * @param spendUTXO
     * @param unSpendUTXO
     * @return
     */
    boolean mergeUTXO(List<ASSET> spendUTXO,List<ASSET> unSpendUTXO);

    /**
     *
     * @param address
     * @param balance
     * @param currency
     * @return
     */
    AccountState createAccountState(String address,BigInteger balance,String currency);

    List<AccountDetail> getAccountDetails();

    /**
     * add utxo into first cache and build Account
     * @return
     */
    boolean addUTXO(ASSET asset);
}
