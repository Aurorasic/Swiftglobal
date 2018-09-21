package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.contract.RepositoryImpl;
import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.GasCost;
import com.higgsblock.global.chain.vm.OpCode;
import com.higgsblock.global.chain.vm.api.ExecutionEnvironment;
import com.higgsblock.global.chain.vm.api.ExecutionResult;
import com.higgsblock.global.chain.vm.api.Executor;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.config.Constants;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.program.Program;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chen Jiawei
 * @date 2018-09-18
 */
public class ExecutorTest {
    private Executor executor;
    private Repository blockRepository;

    @Before
    public void setUp() {
        String transactionHash = "03e22f204d45f061a5b68847534b428a1277652677b6adff2d1f3381bbc4115c";
        boolean isContractCreation = true;
        byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");
        byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
        byte[] gasPrice = BigInteger.valueOf(1_000_000_000L).toByteArray();
        byte[] gasLimit = BigInteger.valueOf(125000L).toByteArray();
        byte[] value = BigInteger.valueOf(0 * 1_000_000_000_000_000_000L).toByteArray();

        byte[] data = Hex.decode("608060405234801561001057600080fd5b5060fa8061001f6000396000f30060806040526004361060525763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663590e1ae38114605757806360fe47b114605f5780636d4ce63c146074575b600080fd5b605d6098565b005b348015606a57600080fd5b50605d60043560c3565b348015607f57600080fd5b50608660c8565b60408051918252519081900360200190f35b6040513390600090600a9082818181858883f1935050505015801560c0573d6000803e3d6000fd5b50565b600055565b600054905600a165627a7a723058207de1f57b6c05faf418f8f4a3566fc2e11137539e3d25b48512bc0e6b8ad176f90029");
      //  data = Hex.decode("590e1ae3");
        //60806040526004361060525763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663590e1ae38114605757806360fe47b114605f5780636d4ce63c146074575b600080fd5b605d6098565b005b348015606a57600080fd5b50605d60043560c3565b348015607f57600080fd5b50608660c8565b60408051918252519081900360200190f35b6040513390600090600a9082818181858883f1935050505015801560c0573d6000803e3d6000fd5b50565b600055565b600054905600a165627a7a723058207de1f57b6c05faf418f8f4a3566fc2e11137539e3d25b48512bc0e6b8ad176f90029
        SystemProperties systemProperties = new SystemProperties() {
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

        BlockchainConfig blockchainConfig = new BlockchainConfig() {
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

        byte[] parentHash = Hex.decode("34801561001057600080fd5b5060bf8061001f6000396000f300608060405260");
        byte[] coinbase = Hex.decode("5060bf8061001f6000396000f300608060405260");
        long timestamp = 1536822282L;
        long number = 1899;
        byte[] difficulty = BigInteger.valueOf(378572L).toByteArray();
        byte[] gasLimitBlock = BigInteger.valueOf(12500000L).toByteArray();
        byte[] balance = BigInteger.valueOf(1_000_000_000_000_000_000L).multiply(BigInteger.valueOf(125L)).toByteArray();

        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(transactionHash, isContractCreation,
                contractAddress, senderAddress, gasPrice, gasLimit, value, data, systemProperties,
                blockchainConfig, parentHash, coinbase, timestamp, number, difficulty, gasLimitBlock, balance);

        blockRepository  = new RepositoryImpl();
        Repository transactionRepository = blockRepository.startTracking();

        executor = new Executor(transactionRepository, executionEnvironment);
    }

    @Test
    public void testExecute() {
        ExecutionResult executionResult = executor.execute();


        // 45 is used for ops fee.
        //Assert.assertEquals(45, executionResult.getGasUsed().intValue());
        //constructor has no payable modifier, check fails.
      //  Assert.assertEquals("REVERT opcode executed.", executionResult.getErrorMessage());
    //    Assert.assertEquals(124955, executionResult.getRemainGas().intValue());
        System.out.println("部署合约代码"+Hex.toHexString(executionResult.getResult()));
      //  Assert.assertNull();
        blockRepository.flush();
    }

    @After
    public void tearDown() {
        executor = null;
    }
}
