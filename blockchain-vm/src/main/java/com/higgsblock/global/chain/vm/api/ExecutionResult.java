package com.higgsblock.global.chain.vm.api;

import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.LogInfo;
import com.higgsblock.global.chain.vm.program.InternalTransaction;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * @author Chen Jiawei
 * @date 2018-09-14
 */
public class ExecutionResult {
    private String transactionHash;
    private String errorMessage;
    private BigInteger remainGas;
    private List<LogInfo> logInfoList;
    private Set<DataWord> deleteAccounts;
    private List<InternalTransaction> internalTransactions;
    private byte[] result;
    private BigInteger gasUsed;
    private BigInteger gasRefund;

    private List<TransferInfo> transferInfoList;

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public BigInteger getRemainGas() {
        return remainGas;
    }

    public void setRemainGas(BigInteger remainGas) {
        this.remainGas = remainGas;
    }

    public void spendGas(BigInteger spentGas) {
        remainGas = remainGas.subtract(spentGas);
    }

    public List<LogInfo> getLogInfoList() {
        return logInfoList;
    }

    public void setLogInfoList(List<LogInfo> logInfoList) {
        this.logInfoList = logInfoList;
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

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

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

    public List<TransferInfo> getTransferInfoList() {
        return transferInfoList;
    }

    public void setTransferInfoList(List<TransferInfo> transferInfoList) {
        this.transferInfoList = transferInfoList;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
