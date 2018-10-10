/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.higgsblock.global.chain.vm.program;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvoke;
import com.higgsblock.global.chain.vm.program.listener.ProgramListener;
import com.higgsblock.global.chain.vm.program.listener.ProgramListenerAware;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public class Storage implements Repository, ProgramListenerAware {

    private final Repository repository;
    private final DataWord address;
    private ProgramListener programListener;

    public Storage(ProgramInvoke programInvoke) {
        this.address = programInvoke.getOwnerAddress();
        this.repository = programInvoke.getRepository();
    }

    @Override
    public void setProgramListener(ProgramListener listener) {
        this.programListener = listener;
    }

    @Override
    public AccountState createAccount(byte[] addr) {
        return repository.createAccount(addr);
    }

    @Override
    public boolean isExist(byte[] addr) {
        return repository.isExist(addr);
    }

    @Override
    public AccountState getAccountState(byte[] addr) {
        return repository.getAccountState(addr);
    }

    @Override
    public long getNonce(byte[] addr) {
        return repository.getNonce(addr);
    }

    @Override
    public long increaseNonce(byte[] addr) {
        return repository.increaseNonce(addr);
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

    @Override
    public void delete(byte[] addr) {
        if (canListenTrace(addr)) programListener.onStorageClear();
        repository.delete(addr);
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return repository.getContractDetails(addr);
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return repository.hasContractDetails(addr);
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        repository.saveCode(addr, code);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        return repository.getCode(addr);
    }

    @Override
    public byte[] getCodeHash(byte[] addr) {
        return repository.getCodeHash(addr);
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        if (canListenTrace(addr)) programListener.onStoragePut(key, value);
        repository.addStorageRow(addr, key, value);
    }

    private boolean canListenTrace(byte[] address) {
        return (programListener != null) && this.address.equals(new DataWord(address));
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return repository.getStorageValue(addr, key);
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        return repository.getBalance(addr);
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        return repository.addBalance(addr, value);
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return repository.getAccountsKeys();
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        repository.dumpState(block, gasUsed, txNumber, txHash);
    }

    @Override
    public Repository startTracking() {
        return repository.startTracking();
    }

    @Override
    public void flush() {
        repository.flush();
    }

    @Override
    public void flushNoReconnect() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void commit() {
        repository.commit();
    }

    @Override
    public void rollback() {
        repository.rollback();
    }

    @Override
    public void syncToRoot(byte[] root) {
        repository.syncToRoot(root);
    }

    @Override
    public boolean isClosed() {
        return repository.isClosed();
    }

    @Override
    public void close() {
        repository.close();
    }

    @Override
    public void reset() {
        repository.reset();
    }

    @Override
    public byte[] getRoot() {
        return new byte[0];
    }


    @Override
    public Repository getSnapshotTo(byte[] root) {
        throw new UnsupportedOperationException();
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
    public List getUnSpendAsset(byte[] address) {
        return null;
    }

    /**
     * get spend asset
     *
     * @param address
     * @return
     */
    @Override
    public List getSpendAsset(byte[] address) {
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
    public boolean mergeUTXO(List spendUTXO, List unSpendUTXO) {
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


}
