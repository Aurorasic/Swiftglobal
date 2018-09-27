package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.contract.RepositoryImpl;
import com.higgsblock.global.chain.app.contract.RepositoryRoot;
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
    Repository transactionRepository;
    byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
    byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");

    @Before
    public void setUp() {
        String transactionHash = "03e22f204d45f061a5b68847534b428a1277652677b6adff2d1f3381bbc4115c";
        boolean isContractCreation = false;


        byte[] gasPrice = BigInteger.valueOf(1_000_000_000L).toByteArray();
        byte[] gasLimit = BigInteger.valueOf(12500000L).toByteArray();
        byte[] value = BigInteger.valueOf(0 * 1_000_000_000_000_000_000L).toByteArray();

        byte[] data = Hex.decode("600a60005560e0604052602260808190527f313131313131313131313131313131313131313131313131313131313131313160a09081527f323100000000000000000000000000000000000000000000000000000000000060c052610067916001919061007a565b5034801561007457600080fd5b50610115565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100bb57805160ff19168380011785556100e8565b828001600101855582156100e8579182015b828111156100e85782518255916020019190600101906100cd565b506100f49291506100f8565b5090565b61011291905b808211156100f457600081556001016100fe565b90565b610345806101246000396000f30060806040526004361061006c5763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663191347df8114610071578063590e1ae3146100cc57806360fe47b1146100d45780636d4ce63c146100ec578063b8c9e4ed14610113575b600080fd5b34801561007d57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526100ca94369492936024939284019190819084018382808284375094975061019d9650505050505050565b005b6100ca6101b4565b3480156100e057600080fd5b506100ca6004356101e0565b3480156100f857600080fd5b506101016101e5565b60408051918252519081900360200190f35b34801561011f57600080fd5b506101286101ec565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561016257818101518382015260200161014a565b50505050905090810190601f16801561018f5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b80516101b0906001906020840190610281565b5050565b6040513390600090600a9082818181858883f193505050501580156101dd573d6000803e3d6000fd5b50565b600055565b6000545b90565b60018054604080516020601f600260001961010087891615020190951694909404938401819004810282018101909252828152606093909290918301828280156102775780601f1061024c57610100808354040283529160200191610277565b820191906000526020600020905b81548152906001019060200180831161025a57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102c257805160ff19168380011785556102ef565b828001600101855582156102ef579182015b828111156102ef5782518255916020019190600101906102d4565b506102fb9291506102ff565b5090565b6101e991905b808211156102fb57600081556001016103055600a165627a7a72305820d5032e64b4be83624bef821c56614cd8e1332bf7c68651daefe73d43f1e623b70029");
//       "6d4ce63c": "get()",
//        "b8c9e4ed": "getStr()",
//                "590e1ae3": "refund()",
//                "60fe47b1": "set(uint256)",
//                "191347df": "setStr(string)"
        data = Hex.decode("6d4ce63c");
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

        blockRepository  = new RepositoryRoot();
         transactionRepository = blockRepository.startTracking();

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


        transactionRepository.commit();
        blockRepository.commit();
        //blockRepository.flush();
        System.out.println("GAS: " + executionResult.getGasUsed());

        SolidityCallResult result = new SolidityContractImpl(abi).callFunction("getStr");
        result.setExecutionResult(executionResult.getResult());
        System.out.println("执行结果: "+ result.getReturnValue());


        System.out.println("部署合约代码"+Hex.toHexString(blockRepository.getCode(contractAddress)));
    }

    @After
    public void tearDown() {
        executor = null;
    }
}
