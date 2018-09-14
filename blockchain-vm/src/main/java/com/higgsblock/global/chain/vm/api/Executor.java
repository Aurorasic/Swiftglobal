package com.higgsblock.global.chain.vm.api;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.PrecompiledContracts;
import com.higgsblock.global.chain.vm.VM;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.program.Program;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvoke;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvokeFactory;
import com.higgsblock.global.chain.vm.util.ByteArraySet;
import org.apache.commons.lang3.tuple.Pair;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * @author Chen Jiawei
 * @date 2018-09-13
 */
public class Executor {
    private Transaction transaction;
    private Block block;
    private Repository transactionRepository;
    private Repository contractRepository;
    protected BlockStore blockStore;
    private ProgramInvokeFactory programInvokeFactory;
    private long gasUsedInTheBlock;
    SystemProperties systemProperties;
    BlockchainConfig blockchainConfig;

    private ByteArraySet touchedContractAddresses = new ByteArraySet();
    BigInteger m_endGas = BigInteger.ZERO;
    private String execError;

    public Program execute() {
        byte[] contractAddress = transaction.getContractAddress();
        Program program = null;
        if (transaction.isContractCreation()) {
            contractRepository.createAccount(contractAddress);


            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(transaction, block, contractRepository, blockStore);

            VM vm = new VM(systemProperties);
            program = new Program(transaction.getData(), programInvoke, transaction, systemProperties);


            contractRepository.addBalance(contractAddress, new BigInteger(1, transaction.getValue()));
            touchedContractAddresses.add(contractAddress);

            if (systemProperties.playVM()) {
                vm.play(program);
            }

        } else {
            PrecompiledContracts.PrecompiledContract precompiledContract = PrecompiledContracts.getContractForAddress(new DataWord(contractAddress), blockchainConfig);

            if (precompiledContract != null) {
                long requiredGas = precompiledContract.getGasForData(transaction.getData());

                BigInteger spendingGas = BigInteger.valueOf(requiredGas);

                if (new BigInteger(1, transaction.getGasLimit()).compareTo(spendingGas) < 0) {
                    execError = ("Out of Gas calling precompiled contract 0x" + Hex.toHexString(contractAddress) +
                            ", required: " + spendingGas + ", left: " + m_endGas);
                    m_endGas = BigInteger.ZERO;
                } else {

                    m_endGas = m_endGas.subtract(spendingGas);

                    Pair<Boolean, byte[]> out = precompiledContract.execute(transaction.getData());

                    if (!out.getLeft()) {
                        execError = ("Error executing precompiled contract 0x" + Hex.toHexString(contractAddress));
                        m_endGas = BigInteger.ZERO;
                    } else {
                        program = new Program();
                        program.setHReturn(out.getRight());
                    }
                }

            } else {

                byte[] code = transactionRepository.getCode(contractAddress);
                if (!isEmpty(code)) {
                    ProgramInvoke programInvoke =
                            programInvokeFactory.createProgramInvoke(transaction, block, contractRepository, blockStore);

                    VM vm = new VM(systemProperties);
                    program = new Program(transactionRepository.getCodeHash(contractAddress), code, programInvoke, transaction, systemProperties);

                    contractRepository.addBalance(contractAddress, new BigInteger(1, transaction.getValue()));
                    touchedContractAddresses.add(contractAddress);


                    if (systemProperties.playVM()) {
                        vm.play(program);
                    }
                }

            }

        }

        return program;
    }
}
