package com.higgsblock.global.chain.vm.api;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.LogInfo;
import com.higgsblock.global.chain.vm.PrecompiledContracts;
import com.higgsblock.global.chain.vm.VM;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.program.Program;
import com.higgsblock.global.chain.vm.program.ProgramResult;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvoke;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvokeFactory;
import com.higgsblock.global.chain.vm.util.ByteArraySet;
import org.apache.commons.lang3.tuple.Pair;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

import static org.apache.commons.lang3.ArrayUtils.getLength;

/**
 * @author Chen Jiawei
 * @date 2018-09-13
 */
public class Executor {
    private Block block;
    private Transaction transaction;
    private Repository transactionRepository;
    private Repository contractRepository;

    private ProgramInvokeFactory programInvokeFactory;
    private long gasUsedInTheBlock;
    BigInteger m_endGas = BigInteger.ZERO;

    SystemProperties systemProperties;
    BlockchainConfig blockchainConfig;

    private String execError;
    private TransactionReceipt receipt;
    List<LogInfo> logs = null;
    private ProgramResult result = new ProgramResult();
    private ByteArraySet touchedContractAddresses = new ByteArraySet();

    private Program program;


    public ExecutionResult execute() {
        byte[] contractAddress = transaction.getContractAddress();
        if (transaction.isContractCreation()) {
            contractRepository.createAccount(contractAddress);


            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(transaction, block, contractRepository);

            VM vm = new VM(systemProperties);
            program = new Program(transaction.getData(), programInvoke, transaction, systemProperties);


            contractRepository.addBalance(contractAddress, new BigInteger(1, transaction.getValue()));
            touchedContractAddresses.add(contractAddress);

            if (systemProperties.playVM()) {
                vm.play(program);
            }

            go();
            return finish();
        } else {
            PrecompiledContracts.PrecompiledContract precompiledContract = PrecompiledContracts.getContractForAddress(new DataWord(contractAddress), blockchainConfig);
            if (precompiledContract != null) {
                ExecutionResult executionResult = executePrecompiledContract(precompiledContract, transaction.getData(), m_endGas, contractAddress);
                m_endGas = executionResult.getRemainGas();
                if (executionResult.getExceptionMessage() != null) {
                    execError = executionResult.getExceptionMessage();
                }
                if (executionResult.getResult() != null) {
                    program.setHReturn(executionResult.getResult());
                }

                go();
                return finish();
            } else {

                byte[] code = transactionRepository.getCode(contractAddress);
                ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(transaction, block, contractRepository);

                VM vm = new VM(systemProperties);
                program = new Program(transactionRepository.getCodeHash(contractAddress), code, programInvoke, transaction, systemProperties);

                contractRepository.addBalance(contractAddress, new BigInteger(1, transaction.getValue()));
                touchedContractAddresses.add(contractAddress);


                if (systemProperties.playVM()) {
                    vm.play(program);
                }

                go();
                return finish();
            }
        }
    }

    private BigInteger toBI(long data) {
        return BigInteger.valueOf(data);
    }

    private BigInteger toBI(byte[] data) {
        return new BigInteger(1, data);
    }

    private ExecutionResult finish() {
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setTx(transaction);
        executionResult.setGasLimit(toBI(transaction.getGasLimit()));
        executionResult.setGasPrice(toBI(transaction.getGasPrice()));
        executionResult.setValue(toBI(transaction.getValue()));
        executionResult.setRemainGas(m_endGas);
        executionResult.setLogInfoList(program.getResult().getLogInfoList());
        executionResult.setResult(program.getResult().getHReturn());


        if (result != null) {
            // Accumulate refunds for suicides
            result.addFutureRefund(result.getDeleteAccounts().size() * blockchainConfig.getGasCost().getSUICIDE_REFUND());
            long gasRefund = Math.min(result.getFutureRefund(), toBI(transaction.getGasLimit()).subtract(m_endGas).longValue() / 2);
            byte[] addr = transaction.isContractCreation() ? transaction.getContractAddress() : transaction.getReceiveAddress();
            m_endGas = m_endGas.add(BigInteger.valueOf(gasRefund));

            executionResult.setGasUsed(toBI(result.getGasUsed()));
            executionResult.setGasRefund(toBI(gasRefund));
            executionResult.setDeleteAccounts(result.getDeleteAccounts());
            executionResult.setInternalTransactions(result.getInternalTransactions());
        }

        return executionResult;
    }

    private void go() {
        try {
            result = program.getResult();
            m_endGas = toBI(transaction.getGasLimit()).subtract(toBI(program.getResult().getGasUsed()));

            if (transaction.isContractCreation() && !result.isRevert()) {
                int returnDataGasValue = getLength(program.getResult().getHReturn()) *
                        blockchainConfig.getGasCost().getCREATE_DATA();
                if (m_endGas.compareTo(BigInteger.valueOf(returnDataGasValue)) < 0) {
                    // Not enough gas to return contract code
                    if (!blockchainConfig.getConstants().createEmptyContractOnOOG()) {
                        program.setRuntimeFailure(Program.Exception.notEnoughSpendingGas("No gas to return just created contract",
                                returnDataGasValue, program));
                        result = program.getResult();
                    }
                    result.setHReturn(new byte[0]);
                } else if (getLength(result.getHReturn()) > blockchainConfig.getConstants().getMAX_CONTRACT_SZIE()) {
                    // Contract size too large
                    program.setRuntimeFailure(Program.Exception.notEnoughSpendingGas("Contract size too large: " + getLength(result.getHReturn()),
                            returnDataGasValue, program));
                    result = program.getResult();
                    result.setHReturn(new byte[0]);
                } else {
                    // Contract successfully created
                    m_endGas = m_endGas.subtract(BigInteger.valueOf(returnDataGasValue));
                    contractRepository.saveCode(transaction.getContractAddress(), result.getHReturn());
                }
            }


            if (result.getException() != null || result.isRevert()) {
                result.getDeleteAccounts().clear();
                result.getLogInfoList().clear();
                result.resetFutureRefund();
                rollback();

                if (result.getException() != null) {
                    throw result.getException();
                } else {
                    execError = ("REVERT opcode executed");
                }
            } else {
                touchedContractAddresses.addAll(result.getTouchedAccounts());
                contractRepository.commit();
            }
        } catch (Throwable e) {

            // TODO: catch whatever they will throw on you !!!
//            https://github.com/ethereum/cpp-ethereum/blob/develop/libethereum/Executive.cpp#L241
            rollback();
            m_endGas = BigInteger.ZERO;
            execError = (e.getMessage());
        }
    }

    private void rollback() {

        contractRepository.rollback();

        // remove touched account
        touchedContractAddresses.remove(
                transaction.isContractCreation() ? transaction.getContractAddress() : transaction.getReceiveAddress());
    }

    private ExecutionResult executePrecompiledContract(PrecompiledContracts.PrecompiledContract precompiledContract, byte[] contractData, BigInteger gasForExecution, byte[] contractAddress) {
        if (precompiledContract == null) {
            throw new ContractExecutionException("Precompiled contract to be executed is null.");
        }

        ExecutionResult executionResult = new ExecutionResult();
        BigInteger executionFee = BigInteger.valueOf(precompiledContract.getGasForData(contractData));
        if (gasForExecution.compareTo(executionFee) < 0) {
            executionResult.setExceptionMessage("Out of gas calling precompiled contract 0x" + Hex.toHexString(contractAddress) + ", required: " + executionFee + ", left: " + gasForExecution);
            executionResult.setRemainGas(BigInteger.ZERO);
            return executionResult;
        }

        Pair<Boolean, byte[]> out = precompiledContract.execute(contractData);

        if (!out.getLeft()) {
            executionResult.setExceptionMessage("Error executing precompiled contract 0x" + Hex.toHexString(contractAddress));
            executionResult.setRemainGas(BigInteger.ZERO);
            return executionResult;
        }

        executionResult.setRemainGas(gasForExecution.subtract(executionFee));
        executionResult.setResult(out.getRight());
        return executionResult;
    }
}
