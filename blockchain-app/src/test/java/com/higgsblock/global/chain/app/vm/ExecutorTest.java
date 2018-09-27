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

        byte[] data = Hex.decode("6060604052600a600055606060405190810160405280602281526020017f313131313131313131313131313131313131313131313131313131313131313181526020017f32310000000000000000000000000000000000000000000000000000000000008152506001908051906020019061007b929190610089565b50341561008457fe5b61012e565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100ca57805160ff19168380011785556100f8565b828001600101855582156100f8579182015b828111156100f75782518255916020019190600101906100dc565b5b5090506101059190610109565b5090565b61012b91905b8082111561012757600081600090555060010161010f565b5090565b90565b6103ad8061013d6000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063191347df14610067578063590e1ae3146100c157806360fe47b1146100cb5780636d4ce63c146100eb578063b8c9e4ed14610111575bfe5b341561006f57fe5b6100bf600480803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919050506101aa565b005b6100c96101c5565b005b34156100d357fe5b6100e96004808035906020019091905050610209565b005b34156100f357fe5b6100fb610214565b6040518082815260200191505060405180910390f35b341561011957fe5b61012161021f565b6040518080602001828103825283818151815260200191508051906020019080838360008314610170575b8051825260208311156101705760208201915060208101905060208303925061014c565b505050905090810190601f16801561019c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b80600190805190602001906101c09291906102c8565b505b50565b3373ffffffffffffffffffffffffffffffffffffffff166108fc600a9081150290604051809050600060405180830381858888f19350505050151561020657fe5b5b565b806000819055505b50565b600060005490505b90565b610227610348565b60018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102bd5780601f10610292576101008083540402835291602001916102bd565b820191906000526020600020905b8154815290600101906020018083116102a057829003601f168201915b505050505090505b90565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061030957805160ff1916838001178555610337565b82800160010185558215610337579182015b8281111561033657825182559160200191906001019061031b565b5b509050610344919061035c565b5090565b602060405190810160405280600081525090565b61037e91905b8082111561037a576000816000905550600101610362565b5090565b905600a165627a7a723058200edbe38e9fac5058d415fda4befe55ffe6ac70d4111d9139653475330e87ef540029");
//        "6d4ce63c": "get()",
//                "590e1ae3": "refund()",
//                "60fe47b1": "set(uint256)"
        data = Hex.decode("b8c9e4ed");
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
