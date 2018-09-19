package com.higgsblock.global.chain.vm.api;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.PrecompiledContracts;
import com.higgsblock.global.chain.vm.VM;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.core.AccountState;
import com.higgsblock.global.chain.vm.core.Repository;
import com.higgsblock.global.chain.vm.core.SystemProperties;
import com.higgsblock.global.chain.vm.core.Transaction;
import com.higgsblock.global.chain.vm.program.Program;
import com.higgsblock.global.chain.vm.program.ProgramResult;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvoke;
import com.higgsblock.global.chain.vm.util.ByteArraySet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Jiawei
 * @date 2018-09-13
 */
public class Executor {
    private Repository transactionRepository;
    private ExecutionEnvironment executionEnvironment;

    private byte[] gasLimit;
    private ExecutionResult executionResult;
    private Repository contractRepository;
    private ExecutionTypeEnum executionType;
    private byte[] contractAddress;
    private SystemProperties systemProperties;
    private byte[] data;
    private ProgramInvoke programInvoke;
    private Transaction transaction;
    private byte[] value;
    private ByteArraySet touchedAccountAddresses;
    private BlockchainConfig blockchainConfig;
    private byte[] senderAddress;
    private List<TransferInfo> transferInfoList;

    public Executor(Repository transactionRepository, ExecutionEnvironment executionEnvironment) {
        this.transactionRepository = transactionRepository;
        this.executionEnvironment = executionEnvironment;
        gasLimit = executionEnvironment.getGasLimit();
        executionResult = new ExecutionResult();
        executionResult.setTransactionHash(executionEnvironment.getTransactionHash());
        executionResult.setRemainGas(convertToBigInteger(gasLimit));
        contractRepository = transactionRepository.startTracking();
        executionType = executionEnvironment.getExecutionType();
        contractAddress = executionEnvironment.getContractAddress();
        systemProperties = executionEnvironment.getSystemProperties();
        data = executionEnvironment.getData();
        programInvoke = executionEnvironment.createProgramInvoke(contractRepository);
        transaction = executionEnvironment.createTransaction();
        value = executionEnvironment.getValue();
        touchedAccountAddresses = new ByteArraySet();
        blockchainConfig = executionEnvironment.getBlockchainConfig();
        senderAddress = executionEnvironment.getSenderAddress();
        transferInfoList = new ArrayList<>();
    }

    public ExecutionResult execute() {
        switch (executionType) {
            case CONTRACT_CREATION: {
                return createContract();
            }
            case CONTRACT_CALL: {
                return callContract();
            }
            case PRECOMPILED_CONTRACT_CALL: {
                return callPrecompiledContract();
            }
            default: {
                throw new ContractExecutionException("Execution type is unknown.");
            }
        }
    }

    private ExecutionResult createContract() {
        contractRepository.createAccount(contractAddress);
        //contractRepository.addBalance(contractAddress, convertToBigInteger(value));
        transferInfoList.add(new TransferInfo(senderAddress, contractAddress, convertToBigInteger(value)));
        touchedAccountAddresses.add(contractAddress);

        VM vm = new VM(systemProperties);
        Program program = new Program(data, programInvoke, transaction, systemProperties);
        program.setTransferInfoList(transferInfoList);
        if (systemProperties.playVM()) {
            vm.play(program);
        }

        ProgramResult programResult = program.getResult();
        try {
            executionResult.spendGas(convertToBigInteger(programResult.getGasUsed()));

            if (!programResult.isRevert()) {
                int contractSaveGas = ArrayUtils.getLength(programResult.getHReturn()) * blockchainConfig.getGasCost().getCREATE_DATA();
                if (executionResult.getRemainGas().compareTo(convertToBigInteger(contractSaveGas)) < 0) {
                    programResult.setException(
                            Program.Exception.notEnoughSpendingGas("No gas to return created contract.", contractSaveGas, program));
                } else if (ArrayUtils.getLength(programResult.getHReturn()) > blockchainConfig.getConstants().getMAX_CONTRACT_SZIE()) {
                    programResult.setException(
                            Program.Exception.notEnoughSpendingGas("Contract size too large: " + ArrayUtils.getLength(programResult.getHReturn()), contractSaveGas, program));
                } else {
                    executionResult.spendGas(convertToBigInteger(contractSaveGas));
                    contractRepository.saveCode(contractAddress, programResult.getHReturn());
                }
            }

            if (programResult.getException() != null || programResult.isRevert()) {
                programResult.getDeleteAccounts().clear();
                programResult.getLogInfoList().clear();
                programResult.resetFutureRefund();
                program.getTransferInfoList().clear();
                rollback();

                if (programResult.getException() != null) {
                    throw programResult.getException();
                } else {
                    executionResult.setErrorMessage("REVERT opcode executed.");
                }
            } else {
                touchedAccountAddresses.addAll(programResult.getTouchedAccounts());
                contractRepository.commit();
            }
        } catch (Throwable e) {
            rollback();
            executionResult.setRemainGas(BigInteger.ZERO);
            executionResult.setErrorMessage(e.getMessage());
        }

        long gasRefund = adjustRefund(programResult, executionResult.getRemainGas());
        executionResult.spendGas(convertToBigInteger(gasRefund).negate());
        executionResult.setGasRefund(convertToBigInteger(gasRefund));
        executionResult.setGasUsed(convertToBigInteger(programResult.getGasUsed()));
        executionResult.setDeleteAccounts(programResult.getDeleteAccounts());
        executionResult.setInternalTransactions(programResult.getInternalTransactions());
        executionResult.setLogInfoList(programResult.getLogInfoList());
        executionResult.setResult(programResult.getHReturn());
        executionResult.setTransferInfoList(transferInfoList);

        for (DataWord address : programResult.getDeleteAccounts()) {
            transactionRepository.delete(address.getLast20Bytes());
        }

        touchedAccountAddresses.add(executionEnvironment.getCoinbase());
        if (blockchainConfig.eip161()) {
            for (byte[] touchedAccountAddress : touchedAccountAddresses) {
                AccountState state = transactionRepository.getAccountState(touchedAccountAddress);
                if (state != null && state.isEmpty()) {
                    transactionRepository.delete(touchedAccountAddress);
                }
            }
        }

        return executionResult;
    }

    private ExecutionResult callContract() {
        //contractRepository.addBalance(contractAddress, convertToBigInteger(value));
        touchedAccountAddresses.add(contractAddress);

        VM vm = new VM(systemProperties);
        Program program = new Program(transactionRepository.getCodeHash(contractAddress),
                transactionRepository.getCode(contractAddress), programInvoke, transaction, systemProperties);
        program.setTransferInfoList(transferInfoList);
        if (systemProperties.playVM()) {
            vm.play(program);
        }

        ProgramResult programResult = program.getResult();
        try {
            executionResult.spendGas(convertToBigInteger(programResult.getGasUsed()));

            if (programResult.getException() != null || programResult.isRevert()) {
                programResult.getDeleteAccounts().clear();
                programResult.getLogInfoList().clear();
                programResult.resetFutureRefund();
                program.getTransferInfoList().clear();
                rollback();

                if (programResult.getException() != null) {
                    throw programResult.getException();
                } else {
                    executionResult.setErrorMessage("REVERT opcode executed.");
                }
            } else {
                touchedAccountAddresses.addAll(programResult.getTouchedAccounts());
                contractRepository.commit();
            }
        } catch (Throwable e) {
            rollback();
            executionResult.setRemainGas(BigInteger.ZERO);
            executionResult.setErrorMessage(e.getMessage());
        }


        long gasRefund = adjustRefund(programResult, executionResult.getRemainGas());
        executionResult.spendGas(convertToBigInteger(gasRefund).negate());
        executionResult.setGasRefund(convertToBigInteger(gasRefund));
        executionResult.setGasUsed(convertToBigInteger(programResult.getGasUsed()));
        executionResult.setDeleteAccounts(programResult.getDeleteAccounts());
        executionResult.setInternalTransactions(programResult.getInternalTransactions());
        executionResult.setLogInfoList(programResult.getLogInfoList());
        executionResult.setResult(programResult.getHReturn());
        executionResult.setTransferInfoList(transferInfoList);

        for (DataWord address : programResult.getDeleteAccounts()) {
            transactionRepository.delete(address.getLast20Bytes());
        }

        touchedAccountAddresses.add(executionEnvironment.getCoinbase());
        if (blockchainConfig.eip161()) {
            for (byte[] touchedAccountAddress : touchedAccountAddresses) {
                AccountState state = transactionRepository.getAccountState(touchedAccountAddress);
                if (state != null && state.isEmpty()) {
                    transactionRepository.delete(touchedAccountAddress);
                }
            }
        }

        return executionResult;
    }

    private ExecutionResult callPrecompiledContract() {
        PrecompiledContracts.PrecompiledContract precompiledContract = executionEnvironment.getPrecompiledContract();
        if (precompiledContract == null) {
            throw new ContractExecutionException("Precompiled contract to be executed is null.");
        }

        BigInteger executionFee = convertToBigInteger(precompiledContract.getGasForData(data));
        if (executionResult.getRemainGas().compareTo(executionFee) < 0) {
            executionResult.setErrorMessage("Out of gas calling precompiled contract 0x"
                    + Hex.toHexString(contractAddress) + ", required: " + executionFee + ", left: " + executionResult.getRemainGas());
            executionResult.setRemainGas(BigInteger.ZERO);
            return executionResult;
        }

        Pair<Boolean, byte[]> out = precompiledContract.execute(data);

        if (!out.getLeft()) {
            executionResult.setErrorMessage("Error executing precompiled contract 0x" + Hex.toHexString(contractAddress));
            executionResult.setRemainGas(BigInteger.ZERO);
            return executionResult;
        }

        executionResult.spendGas(executionFee);
        executionResult.setResult(out.getRight());

        return executionResult;
    }

    private void rollback() {
        contractRepository.rollback();
        touchedAccountAddresses.remove(contractAddress);
    }

    private BigInteger convertToBigInteger(long data) {
        return BigInteger.valueOf(data);
    }

    private BigInteger convertToBigInteger(byte[] data) {
        return new BigInteger(1, data);
    }

    private long adjustRefund(ProgramResult programResult, BigInteger remainGas) {
        long refundOfDeleteAccounts = programResult.getDeleteAccounts().size() * blockchainConfig.getGasCost().getSUICIDE_REFUND();

        return Math.min(programResult.getFutureRefund() + refundOfDeleteAccounts,
                convertToBigInteger(gasLimit).subtract(remainGas).longValue() / 2);
    }
}
