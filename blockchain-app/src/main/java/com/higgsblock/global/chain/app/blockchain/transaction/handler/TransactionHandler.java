package com.higgsblock.global.chain.app.blockchain.transaction.handler;

import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.ITransactionService;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.network.socket.message.IMessage;
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
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yangyi
 * @deta 2018/3/1
 * @description
 */
//@Slf4j
//@Component
//public class TransactionHandler extends BaseMessageHandler<Transaction> {
//
//    @Autowired
//    private ITransactionService transactionService;
//
//    @Override
//    protected boolean valid(IMessage<Transaction> message) {
//        Transaction tx = message.getData();
//
//        //step1 check transaction baseinfo
//        if (!tx.valid()) {
//            LOGGER.info("transaction is invalid:{}", tx.getHash());
//            return false;
//        }
//        //step2 check transaction size
//        if (!tx.sizeAllowed()) {
//            LOGGER.info("Size of the transaction is illegal: {}", tx.getHash());
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    protected void process(IMessage<Transaction> message) {
//        transactionService.receivedTransaction(message.getData());
//
//    }
//}
@Slf4j
@Component
public class TransactionHandler extends BaseMessageHandler<Transaction> {

    @Autowired
    private BlockService blockService;

    @Override
    protected boolean valid(IMessage<Transaction> message) {
        return true;
    }

    @Override
    protected void process(IMessage<Transaction> message) {

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


        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment();
        blockService.fillExecutionEnvironment(message.getData(), executionEnvironment);

        executionEnvironment.setParentHash(parentHash);
        executionEnvironment.setCoinbase(coinbase);
        executionEnvironment.setTimestamp(timestamp);
        executionEnvironment.setNumber(number);
        executionEnvironment.setDifficulty(difficulty);
        executionEnvironment.setGasLimitBlock(gasLimitBlock);
        executionEnvironment.setBalance(balance);

        executionEnvironment.setSystemProperties(systemProperties);
        executionEnvironment.setBlockchainConfig(blockchainConfig);


        Repository transactionRepository = new RepositoryImplTest();

        Executor executor = new Executor(transactionRepository, executionEnvironment);
        ExecutionResult executionResult = executor.execute();

        LOGGER.info(executionResult.toString());
    }

    class RepositoryImplTest implements Repository {
        private Map<byte[], byte[]> db = new HashMap<byte[], byte[]>();

        @Override
        public synchronized long getNonce(byte[] addr) {
            return 0;
        }

        @Override
        public long increaseNonce(byte[] addr) {
            return 0;
        }

        @Override
        public AccountState createAccount(byte[] addr) {
            db.put(addr, new byte[0]);
            System.out.println(db.get(addr) != null);
            return null;
        }

        @Override
        public boolean isExist(byte[] addr) {
            return db.get(addr) != null;
        }

        @Override
        public AccountState getAccountState(byte[] addr) {
            return null;
        }

        @Override
        public AccountState getAccountState(byte[] address, String currency) {
            return null;
        }

        @Override
        public void delete(byte[] addr) {

        }

        @Override
        public ContractDetails getContractDetails(byte[] addr) {
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
            return new RepositoryMockImpl();
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
        public void transfer(byte[] from, byte[] address, BigInteger amount, String currency) {

        }

        @Override
        public List getUnSpendAsset(byte[] address) {
            return null;
        }

        @Override
        public List getSpendAsset(byte[] address) {
            return null;
        }

        @Override
        public boolean mergeUTXO(List spendUTXO, List unSpendUTXO) {
            return false;
        }

        @Override
        public AccountState createAccountState(byte[] address, BigInteger balance, String currency) {
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
}
