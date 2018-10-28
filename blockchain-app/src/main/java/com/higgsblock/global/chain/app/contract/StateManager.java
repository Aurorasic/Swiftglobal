package com.higgsblock.global.chain.app.contract;

/**
 * @author tangkun
 * @date 2018-10-28
 */

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.vm.api.ExecutionResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

/**
 * Constantly modifies block state in the process of packing transactions.
 */
@NoArgsConstructor
@Data
public class StateManager {
    /**
     * size of packaged transactions in a specific block.
     */
    private long totalUsedSize = 0;
    /**
     * amount of gas used by packaged transactions.
     */
    private long totalUsedGas = 0;
    /**
     * fee used by packaged transactions.
     */
    private Money totalFee = new Money();
    /**
     * hash of global state.
     */
    private String globalStateHash = Strings.EMPTY;

    public void addUsedSize(long size) {
        totalUsedSize += size;
    }

    public void addUsedGas(long gas) {
        totalUsedGas += gas;
    }

    public void addFee(Money fee) {
        totalFee = totalFee.add(fee);
    }

    public void subtractFee(Money fee) {
        totalFee = totalFee.subtract(fee);
    }

    public void updateGlobalStateHash(ExecutionResult executionResult) {
        globalStateHash = calculateExecutionHash(globalStateHash, executionResult);
    }

    public void updateGlobalStateHash(RepositoryRoot blockRepository) {
        if (StringUtils.isEmpty(globalStateHash)) {
            globalStateHash = Strings.EMPTY;
        }

        String dbStateHash = blockRepository.getStateHash();
        if (StringUtils.isEmpty(dbStateHash)) {
            dbStateHash = Strings.EMPTY;
        }

        globalStateHash = appendStorageHash(globalStateHash, dbStateHash);
    }

    public String calculateExecutionHash(ExecutionResult executionResult) {
        HashFunction function = Hashing.sha256();
        return function.hashString(executionResult.toString(), Charsets.UTF_8).toString();
    }

    public String appendStorageHash(String blockContractStateHash, String storageHash) {
        HashFunction function = Hashing.sha256();
        return function.hashString(String.join(blockContractStateHash, storageHash), Charsets.UTF_8).toString();
    }

    /**
     * Calculates latest execution result hash.
     *
     * @param currentHash     execution result hash for previous transactions.
     * @param executionResult result related to contract execution procedure.
     * @return latest execution result hash.
     */
    private String calculateExecutionHash(String currentHash, ExecutionResult executionResult) {
        HashFunction function = Hashing.sha256();
        return function.hashString(
                String.join(currentHash, calculateExecutionHash(executionResult)), Charsets.UTF_8).toString();
    }
}