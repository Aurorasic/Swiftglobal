package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.contract.RepositoryImpl;
import com.higgsblock.global.chain.app.contract.RepositoryRoot;
import com.higgsblock.global.chain.app.dao.IContractRepository;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
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
import com.higgsblock.global.chain.vm.solidity.CallTransaction;
import com.higgsblock.global.chain.vm.solidity.SolidityCallResult;
import com.higgsblock.global.chain.vm.solidity.SolidityContractImpl;
import com.higgsblock.global.chain.vm.solidity.SolidityType;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chen Jiawei
 * @date 2018-09-18
 */
@Slf4j
public class ExecutorTest extends BaseTest {

    @Autowired
    private IContractRepository contractRepository;

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    private Executor executor;
    private Repository blockRepository;
    Repository transactionRepository;
    byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
    byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115d");

    @Before
    public void setUp() {
        String transactionHash = "03e22f204d45f061a5b68847534b428a1277652677b6adff2d1f3381bbc4115c";
        boolean isContractCreation = true;


        byte[] gasPrice = BigInteger.valueOf(1_000_000_000L).toByteArray();
        byte[] gasLimit = BigInteger.valueOf(12500000L).toByteArray();
        byte[] value = BigInteger.valueOf(0 * 1_000_000_000_000_000_000L).toByteArray();

        byte[] data = Hex.decode("6080604052600a61000e610064565b90815260405190819003602001906000f080158015610031573d6000803e3d6000fd5b5060008054600160a060020a031916600160a060020a039290921691909117905534801561005e57600080fd5b50610073565b60405160598061027183390190565b6101ef806100826000396000f30060806040526004361061004b5763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663a699a9b68114610050578063f003abfe1461006d575b600080fd5b34801561005c57600080fd5b5061006b600435602435610085565b005b34801561007957600080fd5b5061006b6004356100f1565b808261008f61015b565b908152604051908190036020019082f0801580156100b1573d6000803e3d6000fd5b506000805473ffffffffffffffffffffffffffffffffffffffff191673ffffffffffffffffffffffffffffffffffffffff92909216919091179055505050565b806100fa61015b565b90815260405190819003602001906000f08015801561011d573d6000803e3d6000fd5b506000805473ffffffffffffffffffffffffffffffffffffffff191673ffffffffffffffffffffffffffffffffffffffff9290921691909117905550565b60405160598061016b83390190560060806040526040516020806059833981016040525160005560358060246000396000f3006080604052600080fd00a165627a7a72305820b1ea607496ab26948b45aad998d74ddc6b8f3dea5138f9d8a044a24d7f155d950029a165627a7a72305820fc7f3efb09af7475fd9828af84c6bfd7246bbbbad1b1e7771035c2209e004f25002960806040526040516020806059833981016040525160005560358060246000396000f3006080604052600080fd00a165627a7a72305820b1ea607496ab26948b45aad998d74ddc6b8f3dea5138f9d8a044a24d7f155d950029");
//       "6d4ce63c": "get()",
//        "b8c9e4ed": "getStr()",
//                "590e1ae3": "refund()",
//                "60fe47b1": "set(uint256)",
//                "191347df": "setStr(string)"
        //data = Hex.decode("41c0e1b5");
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

            /**
             * block limit size
             *
             * @return limit size
             */
            @Override
            public int getLimitedSize() {
                return 0;
            }

            /**
             * contract limit size
             *
             * @return limit size
             */
            @Override
            public int getContractLimitedSize() {
                return 0;
            }

            @Override
            public long getBlockGasLimit() {
                return 0;
            }
        };

        byte[] parentHash = Hex.decode("34801561001057600080fd5b5060bf8061001f6000396000f300608060405260");
        byte[] coinbase = Hex.decode("5060bf8061001f6000396000f300608060405260");
        long timestamp = 1536822282L;
        long number = 1899;
        byte[] difficulty = BigInteger.valueOf(378572L).toByteArray();
        byte[] gasLimitBlock = BigInteger.valueOf(125000000L).toByteArray();
        byte[] balance = BigInteger.valueOf(1_000_000_000_000_000_000L).multiply(BigInteger.valueOf(125L)).toByteArray();

        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(transactionHash, isContractCreation,
                contractAddress, senderAddress, gasPrice, gasLimit, value, data, systemProperties,
                blockchainConfig, parentHash, coinbase, timestamp, number, difficulty, gasLimitBlock, balance);

        blockRepository  = new RepositoryRoot(contractRepository, "", utxoServiceProxy, SystemProperties.getDefault());
        transactionRepository = blockRepository.startTracking();

        executor = new Executor(transactionRepository.startTracking(), transactionRepository, executionEnvironment);



    }

    @Test
    public void testExecute() {
        ExecutionResult executionResult = executor.execute();


        // 45 is used for ops fee.
        //Assert.assertEquals(45, executionResult.getGasUsed().intValue());
        //constructor has no payable modifier, check fails.
        //  Assert.assertEquals("REVERT opcode executed.", executionResult.getErrorMessage());
        //    Assert.assertEquals(124955, executionResult.getRemainGas().intValue());

        String abi= "[" +
                "{" +
                "\"constant\": true," +
                "\"inputs\": []," +
                "\"name\": \"getStr\"," +
                "\"outputs\": [" +
                "{" +
                "\"name\": \"retVal\"," +
                "\"type\": \"string\"" +
                "}" +
                "]," +
                "\"payable\": false," +
                "\"type\": \"function\"," +
                "\"stateMutability\": \"view\"" +
                "}," +
                "{" +
                "\"constant\": true," +
                "\"inputs\": []," +
                "\"name\": \"get\"," +
                "\"outputs\": [" +
                "{" +
                "\"name\": \"retVal\"," +
                "\"type\": \"uint256\"" +
                "}" +
                "]," +
                "\"payable\": false," +
                "\"type\": \"function\"," +
                "\"stateMutability\": \"view\"" +
                "}," +
                "{" +
                "\"constant\": false," +
                "\"inputs\": []," +
                "\"name\": \"refund\"," +
                "\"outputs\": []," +
                "\"payable\": true," +
                "\"type\": \"function\"," +
                "\"stateMutability\": \"payable\"" +
                "}," +
                "{" +
                "\"constant\": false," +
                "\"inputs\": [" +
                "{" +
                "\"name\": \"x\"," +
                "\"type\": \"uint256\"" +
                "}" +
                "]," +
                "\"name\": \"set\"," +
                "\"outputs\": []," +
                "\"payable\": false," +
                "\"type\": \"function\"," +
                "\"stateMutability\": \"nonpayable\"" +
                "}," +
                "{" +
                "\"constant\": false," +
                "\"inputs\": [" +
                "{" +
                "\"name\": \"s\"," +
                "\"type\": \"string\"" +
                "}" +
                "]," +
                "\"name\": \"setStr\"," +
                "\"outputs\": []," +
                "\"payable\": false," +
                "\"type\": \"function\"," +
                "\"stateMutability\": \"nonpayable\"" +
                "}" +
                "]";


        //transactionRepository.delete(new DataWord(contractAddress).getLast20Bytes());
        transactionRepository.commit();
        blockRepository.commit();
        //blockRepository.flush();
        System.out.println("GAS: " + executionResult.getGasUsed());

        SolidityCallResult result = new SolidityContractImpl(abi).callFunction("get");
        result.setExecutionResult(executionResult.getResult());
        //System.out.println("执行结果: "+ result.getReturnValue());
        System.out.println("执行结果: "+ Hex.toHexString(executionResult.getResult()));

        System.out.println("部署合约代码"+Hex.toHexString(blockRepository.getCode(contractAddress)));

        AccountState accountState = blockRepository.getAccountState(contractAddress);

        System.out.println("accountState: "+accountState);

        System.out.println("all : "+ contractRepository.findAll());


    }

    @After
    public void tearDown() {
        executor = null;
    }
}
