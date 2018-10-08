package com.higgsblock.global.chain.vm.api;

import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.LogInfo;
import com.higgsblock.global.chain.vm.program.InternalTransaction;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * Result of contract execution.
 *
 * @author Chen Jiawei
 * @date 2018-09-14
 */
@Data
public class ExecutionResult {
    /**
     * Hash of transaction which contains the contract.
     */
    private String transactionHash;
    /**
     * Error information, null if contract is executed successfully.
     */
    private String errorMessage;
    /**
     * Gas amount that shall be returned to sender or senders.
     */
    private BigInteger remainGas;
    /**
     * Returned event records.
     */
    private List<LogInfo> logInfoList;
    /**
     * Deleted account address during contract execution.
     */
    private Set<DataWord> deleteAccounts;
    /**
     * Internal transaction happened during contract execution.
     */
    private List<InternalTransaction> internalTransactions;
    /**
     * Byte code stored after contract execution.
     */
    private byte[] result;
    /**
     * Gas amount used for contract execution.
     */
    private BigInteger gasUsed;
    /**
     * Refunded gas.
     */
    private BigInteger gasRefund;
    /**
     * Transfer records during contract execution.
     */
    private List<TransferInfo> transferInfoList;

    /**
     * Spends specific gas from remained gas.
     *
     * @param spentGas spent gas amount.
     */
    public void spendGas(BigInteger spentGas) {
        remainGas = remainGas.subtract(spentGas);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
