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
package com.higgsblock.global.chain.vm;

import com.higgsblock.global.chain.vm.core.Transaction;
import com.higgsblock.global.chain.vm.util.ByteUtil;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.higgsblock.global.chain.vm.util.ByteUtil.EMPTY_BYTE_ARRAY;


/**
 * The transaction receipt is a tuple of three items
 * comprising the transaction, together with the post-transaction state,
 * and the cumulative gas used in the block containing the transaction receipt
 * as of immediately after the transaction has happened,
 */
public class TransactionReceipt {

    private Transaction transaction;

    private byte[] postTxState = EMPTY_BYTE_ARRAY;
    private byte[] cumulativeGas = EMPTY_BYTE_ARRAY;

    private List<LogInfo> logInfoList = new ArrayList<>();

    private byte[] gasUsed = EMPTY_BYTE_ARRAY;
    private byte[] executionResult = EMPTY_BYTE_ARRAY;
    private String error = "";

    /* Tx Receipt in encoded form */
    private byte[] rlpEncoded;

    public TransactionReceipt() {
    }



    public byte[] getPostTxState() {
        return postTxState;
    }

    public byte[] getCumulativeGas() {
        return cumulativeGas;
    }

    public byte[] getGasUsed() {
        return gasUsed;
    }

    public byte[] getExecutionResult() {
        return executionResult;
    }

    public long getCumulativeGasLong() {
        return new BigInteger(1, cumulativeGas).longValue();
    }




    public List<LogInfo> getLogInfoList() {
        return logInfoList;
    }

    public boolean isValid() {
        return ByteUtil.byteArrayToLong(gasUsed) > 0;
    }

    public boolean isSuccessful() {
        return error.isEmpty();
    }

    public String getError() {
        return error;
    }

    /**
     *  Used for Receipt trie hash calculation. Should contain only the following items encoded:
     *  [postTxState, cumulativeGas, bloomFilter, logInfoList]
     */




    public boolean hasTxStatus() {
        return postTxState != null && postTxState.length <= 1;
    }

    public boolean isTxStatusOK() {
        return postTxState != null && postTxState.length == 1 && postTxState[0] == 1;
    }

    public void setCumulativeGas(long cumulativeGas) {
        this.cumulativeGas = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(cumulativeGas));
        rlpEncoded = null;
    }

    public void setCumulativeGas(byte[] cumulativeGas) {
        this.cumulativeGas = cumulativeGas;
        rlpEncoded = null;
    }

    public void setGasUsed(byte[] gasUsed) {
        this.gasUsed = gasUsed;
        rlpEncoded = null;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(gasUsed));
        rlpEncoded = null;
    }

    public void setExecutionResult(byte[] executionResult) {
        this.executionResult = executionResult;
        rlpEncoded = null;
    }

    public void setError(String error) {
        this.error = error == null ? "" : error;
    }

    public void setLogInfoList(List<LogInfo> logInfoList) {
        if (logInfoList == null) return;
        this.logInfoList = logInfoList;

        for (LogInfo loginfo : logInfoList) {
          //  bloomFilter.or(loginfo.getBloom());
        }
        rlpEncoded = null;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        if (transaction == null) throw new NullPointerException("Transaction is not initialized. Use TransactionInfo and BlockStore to setup Transaction instance");
        return transaction;
    }




}
