package com.higgsblock.global.chain.vm;

import com.higgsblock.global.chain.vm.api.ExecutionEnvironment;
import com.higgsblock.global.chain.vm.api.ExecutionResult;
import com.higgsblock.global.chain.vm.api.Executor;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.config.Constants;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.datasource.Source;
import com.higgsblock.global.chain.vm.program.Program;
import org.junit.After;
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

    @Before
    public void setUp() {
        String transactionHash = "03e22f204d45f061a5b68847534b428a1277652677b6adff2d1f3381bbc4115c";
        boolean isContractCreation = true;
        byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");
        byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
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
        byte[] data = Hex.decode("608060405234801561001057600080fd5b5060bf8061001f6000396000f30060806040526004361060485" +
                "763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166360fe47b18114604d578" +
                "0636d4ce63c146064575b600080fd5b348015605857600080fd5b5060626004356088565b005b348015606f57600080fd5b506" +
                "076608d565b60408051918252519081900360200190f35b600055565b600054905600a165627a7a72305820e1d0b14af22a8bc" +
                "992cb2f3788c2ae1d260f6c4ff559b49864d0e5577e20408f0029");

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

        class RepositoryImplTest implements Repository {
            private Map<byte[], byte[]> db = new HashMap<byte[], byte[]>();

            @Override
            public AccountState createAccount(byte[] addr) {
                return null;
            }

            @Override
            public boolean isExist(byte[] addr) {
                return false;
            }

            @Override
            public AccountState getAccountState(byte[] addr) {
                return null;
            }

            @Override
            public AccountState getAccountState(String address, String currency) {
                return null;
            }

            @Override
            public void delete(byte[] addr) {

            }

            @Override
            public Source<DataWord, DataWord> getContractDetails(byte[] addr) {
                return null;
            }

            @Override
            public boolean hasContractDetails(byte[] addr) {
                return false;
            }

            @Override
            public void saveCode(byte[] addr, byte[] code) {

            }

            @Override
            public byte[] getCode(byte[] addr) {
                return new byte[0];
            }

            @Override
            public byte[] getCodeHash(byte[] addr) {
                return new byte[0];
            }

            @Override
            public void addStorageRow(byte[] addr, DataWord key, DataWord value) {

            }

            @Override
            public DataWord getStorageValue(byte[] addr, DataWord key) {
                return null;
            }

            @Override
            public BigInteger getBalance(byte[] addr) {
                return null;
            }

            @Override
            public BigInteger addBalance(byte[] addr, BigInteger value) {
                return null;
            }

            @Override
            public Set<byte[]> getAccountsKeys() {
                return null;
            }

            @Override
            public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {

            }

            @Override
            public Repository startTracking() {
                return null;
            }

            @Override
            public void flush() {

            }

            @Override
            public void flushNoReconnect() {

            }

            @Override
            public void commit() {

            }

            @Override
            public void rollback() {

            }

            @Override
            public void syncToRoot(byte[] root) {

            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public void reset() {

            }

            @Override
            public byte[] getRoot() {
                return new byte[0];
            }

            @Override
            public Repository getSnapshotTo(byte[] root) {
                return null;
            }

            @Override
            public String getBlockHashByNumber(long blockNumber, String branchBlockHash) {
                return null;
            }

            @Override
            public void transfer(String from, String address, String amount, String currency) {

            }

            @Override
            public List getUnSpendAsset(String address) {
                return null;
            }

            @Override
            public List getSpendAsset(String address) {
                return null;
            }

            @Override
            public boolean mergeUTXO(List spendUTXO, List unSpendUTXO) {
                return false;
            }

            @Override
            public AccountState createAccountState(String address, BigInteger balance, String currency) {
                return null;
            }

            @Override
            public List<AccountDetail> getAccountDetails() {
                return null;
            }

            @Override
            public boolean addUTXO(Object o) {
                return false;
            }
        }

        Repository transactionRepository = new RepositoryImplTest();

        executor = new Executor(transactionRepository, executionEnvironment);
    }

    @Test
    public void testExecute() {
        ExecutionResult executionResult = executor.execute();
    }

    @After
    public void tearDown() {
        executor = null;
    }
}
