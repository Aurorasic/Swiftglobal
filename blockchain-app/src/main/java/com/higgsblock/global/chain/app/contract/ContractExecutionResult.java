package com.higgsblock.global.chain.app.contract;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Records status after contract being executed.
 *
 * @author Chen Jiawei
 * @date 2018-09-27
 */
@Data
@AllArgsConstructor
@JSONType(includes = {"resultHash", "subTransactionList", "minerSignature"})
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
}
