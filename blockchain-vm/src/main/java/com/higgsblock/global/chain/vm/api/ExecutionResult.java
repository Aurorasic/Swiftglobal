package com.higgsblock.global.chain.vm.api;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.LogInfo;
import com.higgsblock.global.chain.vm.core.Transaction;
import com.higgsblock.global.chain.vm.program.InternalTransaction;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * @author Chen Jiawei
 * @date 2018-09-14
 */
public class ExecutionResult {
    private BigInteger remainGas;
    private String exceptionMessage;
    private byte[] result;

    private Transaction tx;
    private BigInteger gasLimit;
    private BigInteger gasPrice;
    private BigInteger value;

    private  BigInteger gasLeftover;
    private List<LogInfo> logInfoList;

    private BigInteger gasUsed;
    private BigInteger gasRefund;
    private Set<DataWord> deleteAccounts;
    private List<InternalTransaction> internalTransactions;

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    public BigInteger getGasRefund() {
        return gasRefund;
    }

    public void setGasRefund(BigInteger gasRefund) {
        this.gasRefund = gasRefund;
    }

    public Set<DataWord> getDeleteAccounts() {
        return deleteAccounts;
    }

    public void setDeleteAccounts(Set<DataWord> deleteAccounts) {
        this.deleteAccounts = deleteAccounts;
    }

    public List<InternalTransaction> getInternalTransactions() {
        return internalTransactions;
    }

    public void setInternalTransactions(List<InternalTransaction> internalTransactions) {
        this.internalTransactions = internalTransactions;
    }

    public Transaction getTx() {
        return tx;
    }

    public void setTx(Transaction tx) {
        this.tx = tx;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public BigInteger getGasLeftover() {
        return gasLeftover;
    }

    public void setGasLeftover(BigInteger gasLeftover) {
        this.gasLeftover = gasLeftover;
    }

    public List<LogInfo> getLogInfoList() {
        return logInfoList;
    }

    public void setLogInfoList(List<LogInfo> logInfoList) {
        this.logInfoList = logInfoList;
    }

    public BigInteger getRemainGas() {
        return remainGas;
    }

    public void setRemainGas(BigInteger remainGas) {
        this.remainGas = remainGas;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }
}
