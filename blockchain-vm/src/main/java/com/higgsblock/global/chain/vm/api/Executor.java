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
    private Transaction tx;
    private Repository cacheTrack;
    SystemProperties config;
    private Block currentBlock;
    private Repository track;
    private ProgramInvokeFactory programInvokeFactory;
    BlockchainConfig blockchainConfig;
    BigInteger m_endGas = BigInteger.ZERO;

    private ProgramResult result = new ProgramResult();
    private Program program;
    List<LogInfo> logs = null;
    private String execError;
    private ByteArraySet touchedAccounts = new ByteArraySet();

    private ExecutionEnvironment executionEnvironment;

    public Executor(ExecutionEnvironment executionEnvironment, Transaction tx, Repository cacheTrack, SystemProperties config, Block currentBlock,
                    Repository track, ProgramInvokeFactory programInvokeFactory,
                    BlockchainConfig blockchainConfig, BigInteger m_endGas) {
        this.executionEnvironment = executionEnvironment;
        this.tx = tx;
        this.cacheTrack = cacheTrack;
        this.config = config;
        this.currentBlock = currentBlock;
        this.track = track;
        this.programInvokeFactory = programInvokeFactory;
        this.blockchainConfig = blockchainConfig;
        this.m_endGas = m_endGas;
    }

    public ExecutionResult execute() {
        if (tx.isContractCreation()) {
            return createContract();
        } else {
            byte[] contractAddress = tx.getContractAddress();
            PrecompiledContracts.PrecompiledContract precompiledContract = PrecompiledContracts.getContractForAddress(new DataWord(contractAddress), blockchainConfig);
            if (precompiledContract != null) {
                return callPrecompiledContract(precompiledContract, tx.getData(), m_endGas, contractAddress);
            } else {
                return callContract();
            }
        }
    }

    private ExecutionResult createContract() {

        byte[] contractAddress = tx.getContractAddress();
        cacheTrack.createAccount(contractAddress);

        ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack);
        VM vm = new VM(config);
        program = new Program(tx.getData(), programInvoke, tx, config);

        cacheTrack.addBalance(contractAddress, new BigInteger(1, tx.getValue()));
        touchedAccounts.add(tx.getContractAddress());

        if (config.playVM()) {
            vm.play(program);
        }

        return finish();
    }

    private ExecutionResult callPrecompiledContract(PrecompiledContracts.PrecompiledContract precompiledContract, byte[] contractData, BigInteger gasForExecution, byte[] contractAddress) {
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


        m_endGas = executionResult.getRemainGas();
        if (executionResult.getExceptionMessage() != null) {
            execError = executionResult.getExceptionMessage();
        }
        if (executionResult.getResult() != null) {
            program.setHReturn(executionResult.getResult());
        }


        return finish();
    }

    private ExecutionResult callContract() {
        byte[] contractAddress = tx.getContractAddress();
        byte[] code = track.getCode(contractAddress);
        ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack);
        VM vm = new VM(config);
        program = new Program(track.getCodeHash(contractAddress), code, programInvoke, tx, config);

        cacheTrack.addBalance(contractAddress, new BigInteger(1, tx.getValue()));
        touchedAccounts.add(contractAddress);


        if (config.playVM()) {
            vm.play(program);
        }

        return finish();
    }

    private BigInteger toBI(long data) {
        return BigInteger.valueOf(data);
    }

    private BigInteger toBI(byte[] data) {
        return new BigInteger(1, data);
    }

    private ExecutionResult finish() {

        try {
            result = program.getResult();
            m_endGas = toBI(tx.getGasLimit()).subtract(toBI(program.getResult().getGasUsed()));

            if (tx.isContractCreation() && !result.isRevert()) {
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
                    cacheTrack.saveCode(tx.getContractAddress(), result.getHReturn());
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
                touchedAccounts.addAll(result.getTouchedAccounts());
                cacheTrack.commit();
            }
        } catch (Throwable e) {

            // TODO: catch whatever they will throw on you !!!
//            https://github.com/ethereum/cpp-ethereum/blob/develop/libethereum/Executive.cpp#L241
            rollback();
            m_endGas = BigInteger.ZERO;
            execError = (e.getMessage());
        }


        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setTx(tx);
        executionResult.setGasLimit(toBI(tx.getGasLimit()));
        executionResult.setGasPrice(toBI(tx.getGasPrice()));
        executionResult.setValue(toBI(tx.getValue()));
        executionResult.setRemainGas(m_endGas);
        executionResult.setLogInfoList(program.getResult().getLogInfoList());
        executionResult.setResult(program.getResult().getHReturn());


        if (result != null) {
            // Accumulate refunds for suicides
            result.addFutureRefund(result.getDeleteAccounts().size() * blockchainConfig.getGasCost().getSUICIDE_REFUND());
            long gasRefund = Math.min(result.getFutureRefund(), toBI(tx.getGasLimit()).subtract(m_endGas).longValue() / 2);
            m_endGas = m_endGas.add(BigInteger.valueOf(gasRefund));

            executionResult.setGasUsed(toBI(result.getGasUsed()));
            executionResult.setGasRefund(toBI(gasRefund));
            executionResult.setDeleteAccounts(result.getDeleteAccounts());
            executionResult.setInternalTransactions(result.getInternalTransactions());
        }

        touchedAccounts.add(currentBlock.getCoinbase());
        if (result != null) {
            logs = result.getLogInfoList();
            // Traverse list of suicides
            for (DataWord address : result.getDeleteAccounts()) {
                track.delete(address.getLast20Bytes());
            }
        }

        if (blockchainConfig.eip161()) {
            for (byte[] acctAddr : touchedAccounts) {
                AccountState state = track.getAccountState(acctAddr);
                if (state != null && state.isEmpty()) {
                    track.delete(acctAddr);
                }
            }
        }

        return executionResult;
    }

    private void rollback() {

        cacheTrack.rollback();

        // remove touched account
        touchedAccounts.remove(
                tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress());
    }
}
