package com.higgsblock.global.chain.vm.api;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.PrecompiledContracts;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.core.Repository;
import com.higgsblock.global.chain.vm.core.SystemProperties;
import com.higgsblock.global.chain.vm.core.Transaction;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvoke;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvokeImpl;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Chen Jiawei
 * @date 2018-09-17
 */
@NoArgsConstructor
@Data
public class ExecutionEnvironment {
    /**
     * Hash of transaction which contains the contract.
     */
    private String transactionHash;
    /**
     * If the transaction contains contract creation.
     */
    private boolean isContractCreation;
    /**
     * Contract address.
     */
    private byte[] contractAddress;
    /**
     * Address of sender(s) who emits the transaction.
     */
    private byte[] senderAddress;
    /**
     * Gas price sender is willing pay.
     */
    private byte[] gasPrice;
    /**
     * Maximum gas amount sender is willing pay.
     */
    private byte[] gasLimit;
    /**
     * Money transferred to contract.
     */
    private byte[] value;
    /**
     * Payload, simple to say is byte code.
     */
    private byte[] data;
    /**
     * Size gas.
     */
    private long sizeGas;

    /**
     * Behave configuration of system.
     */
    private SystemProperties systemProperties;
    /**
     * onfiguration of this block.
     */
    private BlockchainConfig blockchainConfig;
    private PrecompiledContracts.PrecompiledContract precompiledContract;

    private byte[] parentHash;
    private byte[] coinbase;
    private long timestamp;
    private long number;
    private byte[] difficulty;
    private byte[] gasLimitBlock;
    private byte[] balance;

    public ExecutionEnvironment(String transactionHash, boolean isContractCreation, byte[] contractAddress,
                                byte[] senderAddress, byte[] gasPrice, byte[] gasLimit, byte[] value, byte[] data,
                                SystemProperties systemProperties, BlockchainConfig blockchainConfig,
                                byte[] parentHash, byte[] coinbase, long timestamp, long number, byte[] difficulty,
                                byte[] gasLimitBlock, byte[] balance) {
        this.transactionHash = transactionHash;
        this.isContractCreation = isContractCreation;
        this.contractAddress = contractAddress;
        this.senderAddress = senderAddress;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.value = value;
        this.data = data;
        this.systemProperties = systemProperties;
        this.blockchainConfig = blockchainConfig;
        this.parentHash = parentHash;
        this.coinbase = coinbase;
        this.timestamp = timestamp;
        this.number = number;
        this.difficulty = difficulty;
        this.gasLimitBlock = gasLimitBlock;
        this.balance = balance;
    }

    public ExecutionTypeEnum getExecutionType() {
        if (isContractCreation) {
            return ExecutionTypeEnum.CONTRACT_CREATION;
        }

        precompiledContract = PrecompiledContracts.getContractForAddress(new DataWord(contractAddress), blockchainConfig);
        if (precompiledContract != null) {
            return ExecutionTypeEnum.PRECOMPILED_CONTRACT_CALL;
        }

        return ExecutionTypeEnum.CONTRACT_CALL;
    }

    public PrecompiledContracts.PrecompiledContract getPrecompiledContract() {
        if (precompiledContract == null) {
            precompiledContract = PrecompiledContracts.getContractForAddress(new DataWord(contractAddress), blockchainConfig);
        }
        return precompiledContract;
    }

    public Transaction createTransaction() {
        return new Transaction(isContractCreation, contractAddress, senderAddress, gasPrice, gasLimit, value, data);
    }

    public ProgramInvoke createProgramInvoke(Repository contractRepository) {
        return new ProgramInvokeImpl(contractAddress, senderAddress, senderAddress, balance, gasPrice, gasLimit,
                value, data, parentHash, coinbase, timestamp, number, difficulty, gasLimitBlock, contractRepository);
    }
}
