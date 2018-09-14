package com.higgsblock.global.chain.vm.api;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.GasCost;
import com.higgsblock.global.chain.vm.OpCode;
import com.higgsblock.global.chain.vm.VM;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.config.Constants;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.program.Program;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvoke;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvokeFactory;
import com.higgsblock.global.chain.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * @author Chen Jiawei
 * @date 2018-09-12
 */
public class ContractContext {
    public static void main(String[] args) {
        ContractContext contractContext = new ContractContext();
        contractContext.run();
    }

    private void run() {
        Transaction transactionClone = getTransaction();
        ProgramInvoke programInvoke = getProgramInvoke();
        SystemProperties systemProperties = getSystemProperties();
        BlockchainConfig blockchainConfig = getBlockchainConfig();

        Program program = new Program(transactionClone.getData(), programInvoke, transactionClone, systemProperties) {
            @Override
            public void saveOpTrace() {
            }

            public BlockchainConfig getBlockchainConfig() {
                return blockchainConfig;
            }
        };

        VM vm = new VM(systemProperties);
        vm.play(program);

    }

    private BlockchainConfig getBlockchainConfig() {
        return new BlockchainConfig() {
            @Override
            public Constants getConstants() {
                return new Constants();
            }

            @Override
            public boolean eip206() {
                return true;
            }

            @Override
            public boolean eip211() {
                return true;
            }

            @Override
            public boolean eip212() {
                return true;
            }

            @Override
            public boolean eip213() {
                return true;
            }

            @Override
            public boolean eip214() {
                return true;
            }

            @Override
            public boolean eip658() {
                return true;
            }

            @Override
            public GasCost getGasCost() {
                return new GasCost();
            }

            @Override
            public boolean eip161() {
                return true;
            }

            @Override
            public boolean eip198() {
                return true;
            }

            @Override
            public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
                if (requestedGas.compareTo(availableGas) > 0) {
                    throw Program.Exception.notEnoughOpGas(op, requestedGas, availableGas);
                }
                return requestedGas.clone();
            }
        };
    }

    private SystemProperties getSystemProperties() {
        return new SystemProperties() {
            @Override
            public boolean vmTrace() {
                return false;
            }

            @Override
            public int dumpBlock() {
                return 1898;
            }

            @Override
            public String dumpStyle() {
                return "pretty";
            }
        };
    }

    private Transaction getTransaction() {
        boolean isCreate = true;
        byte[] receiveAddress = Hex.decode("d5b5060bf8061001f6000396000f300608060405");
        byte[] sendAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
        byte[] gasPrice = BigInteger.valueOf(1_000_000_000L).toByteArray();
        byte[] gasLimit = BigInteger.valueOf(125000L).toByteArray();
        byte[] value = BigInteger.valueOf(5 * 1_000_000_000_000_000_000L).toByteArray();

//        pragma solidity ^0.4.11;
//
//        contract DataStorage {
//            uint256 data;
//
//            function set(uint256 x) public {
//                data = x;
//            }
//
//            function get() public constant returns (uint256 retVal) {
//                return data;
//            }
//        }
        byte[] data = Hex.decode("608060405234801561001057600080fd5b5060bf8061001f6000396000f30060806040526004361060485763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166360fe47b18114604d5780636d4ce63c146064575b600080fd5b348015605857600080fd5b5060626004356088565b005b348015606f57600080fd5b506076608d565b60408051918252519081900360200190f35b600055565b600054905600a165627a7a72305820e1d0b14af22a8bc992cb2f3788c2ae1d260f6c4ff559b49864d0e5577e20408f0029");

        return new Transaction(isCreate, receiveAddress, sendAddress, gasPrice, gasLimit, value, data);
    }

    private Block getBlock() {
        byte[] parentHash = Hex.decode("34801561001057600080fd5b5060bf8061001f6000396000f300608060405260");
        byte[] coinbase = Hex.decode("5060bf8061001f6000396000f300608060405260");
        long timestamp = 1536822282L;
        long number = 1899;
        byte[] difficulty = BigInteger.valueOf(378572L).toByteArray();
        byte[] gasLimit = BigInteger.valueOf(12500000L).toByteArray();

        return new Block(parentHash, coinbase, timestamp, number, difficulty, gasLimit);
    }

    private ProgramInvoke getProgramInvoke() {
        Transaction transaction = getTransaction();
        Block block = getBlock();
        Repository contractRepository = new RepositoryImpl();

        ProgramInvokeFactory programInvokeFactory = new ProgramInvokeFactoryImpl();
        return programInvokeFactory.createProgramInvoke(transaction, block, contractRepository);
    }
}
