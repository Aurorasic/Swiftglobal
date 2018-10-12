package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.contract.RepositoryRoot;
import com.higgsblock.global.chain.app.dao.IContractRepository;
import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.GasCost;
import com.higgsblock.global.chain.vm.OpCode;
import com.higgsblock.global.chain.vm.api.ExecutionEnvironment;
import com.higgsblock.global.chain.vm.api.ExecutionResult;
import com.higgsblock.global.chain.vm.api.Executor;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.config.Constants;
import com.higgsblock.global.chain.vm.core.AccountState;
import com.higgsblock.global.chain.vm.core.Repository;
import com.higgsblock.global.chain.vm.core.SystemProperties;
import com.higgsblock.global.chain.vm.program.Program;
import com.higgsblock.global.chain.vm.solidity.SolidityCallResult;
import com.higgsblock.global.chain.vm.solidity.SolidityContractImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;

/**
 * @author Chen Jiawei
 * @date 2018-09-18
 */
@Slf4j
public class ExecutorTest extends BaseTest {

    @Autowired
    private IContractRepository contractRepository;

    private Executor executor;
    private Repository blockRepository;
    Repository transactionRepository;
    byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
    byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");

    @Before
    public void setUp() {
        String transactionHash = "03e22f204d45f061a5b68847534b428a1277652677b6adff2d1f3381bbc4115c";
        boolean isContractCreation = true;


        byte[] gasPrice = BigInteger.valueOf(1_000_000_000L).toByteArray();
        byte[] gasLimit = BigInteger.valueOf(12500000L).toByteArray();
        byte[] value = BigInteger.valueOf(0 * 1_000_000_000_000_000_000L).toByteArray();

        byte[] data = Hex.decode("6080604052600a60005534801561001557600080fd5b5060018054600160a060020a03191633179055610148806100376000396000f3006080604052600436106100565763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166341c0e1b5811461005b578063a87d942c14610072578063d09de08a14610099575b600080fd5b34801561006757600080fd5b506100706100ae565b005b34801561007e57600080fd5b506100876100eb565b60408051918252519081900360200190f35b3480156100a557600080fd5b506100706100f1565b60015473ffffffffffffffffffffffffffffffffffffffff163314156100e95760015473ffffffffffffffffffffffffffffffffffffffff16ff5b565b60005490565b60015473ffffffffffffffffffffffffffffffffffffffff163314156100e9576000805460010190555600a165627a7a72305820ad81055b396072bca48db2d8e4ab7b9ce72590fbe6202528137ee99a66dce23f0029");
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

        blockRepository = new RepositoryRoot(contractRepository, "", null, SystemProperties.getDefault());
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

        String abi = "[" +
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
        LOGGER.info("GAS: " + executionResult.getGasUsed());

        SolidityCallResult result = new SolidityContractImpl(abi).callFunction("get");
        result.setExecutionResult(executionResult.getResult());
        //System.out.println("执行结果: "+ result.getReturnValue());
        LOGGER.info("执行结果: " + Hex.toHexString(executionResult.getResult()));

        LOGGER.info("部署合约代码" + Hex.toHexString(blockRepository.getCode(contractAddress)));

        AccountState accountState = blockRepository.getAccountState(contractAddress);

        LOGGER.info("accountState: " + accountState);

        LOGGER.info("all : " + contractRepository.findAll());


    }

    @After
    public void tearDown() {
        executor = null;
    }
}
