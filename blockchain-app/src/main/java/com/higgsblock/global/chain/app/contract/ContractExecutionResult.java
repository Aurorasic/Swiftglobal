package com.higgsblock.global.chain.app.contract;

import java.util.List;

/**
 * Records status after contract being executed.
 *
 * @author Chen Jiawei
 * @date 2018-09-27
 */
public class ContractExecutionResult {
    /**
     * Hash of global difference after contract being executed.
     */
    private String resultHash;
    /**
     * Hash list of new utxo transactions.
     */
    private List<String> subTransactionList;
    /**
     * signature signed by miner after contract being executed.
     */
    private String minerSignature;

    public ContractExecutionResult(String resultHash, List<String> subTransactionList, String minerSignature) {
        this.resultHash = resultHash;
        this.subTransactionList = subTransactionList;
        this.minerSignature = minerSignature;
    }

    public String getResultHash() {
        return resultHash;
    }

    public void setResultHash(String resultHash) {
        this.resultHash = resultHash;
    }

    public List<String> getSubTransactionList() {
        return subTransactionList;
    }

    public void setSubTransactionList(List<String> subTransactionList) {
        this.subTransactionList = subTransactionList;
    }

    public String getMinerSignature() {
        return minerSignature;
    }

    public void setMinerSignature(String minerSignature) {
        this.minerSignature = minerSignature;
    }
}
