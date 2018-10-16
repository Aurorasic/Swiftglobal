package com.higgsblock.global.chain.vm;

import com.higgsblock.global.chain.vm.api.ExecutionEnvironment;
import com.higgsblock.global.chain.vm.api.ExecutionResult;
import com.higgsblock.global.chain.vm.api.Executor;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.config.Constants;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.program.Program;
import org.junit.*;
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
@Ignore
public class ExecutorTest {
    private Executor executor;

    @Before
    public void setUp() {
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
        byte[] data = Hex.decode("608060405234801561001057600080fd5b5060bf8061001f6000396000f300608060405260043610604" +
                "85763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166360fe47b18114604" +
                "d5780636d4ce63c146064575b600080fd5b348015605857600080fd5b5060626004356088565b005b348015606f57600080f" +
                "d5b506076608d565b60408051918252519081900360200190f35b600055565b600054905600a165627a7a72305820e1d0b14" +
                "af22a8bc992cb2f3788c2ae1d260f6c4ff559b49864d0e5577e20408f0029");
        byte[] value = BigInteger.valueOf(5 * 1_000_000_000_000_000_000L).toByteArray();
        byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");
        byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
        setExecutor(data, value, contractAddress, senderAddress);
    }

    private void setExecutor(byte[] data, byte[] value, byte[] contractAddress, byte[] senderAddress) {
        String transactionHash = "03e22f204d45f061a5b68847534b428a1277652677b6adff2d1f3381bbc4115c";
        boolean isContractCreation = true;
        byte[] gasPrice = BigInteger.valueOf(1_000_000_000L).toByteArray();
        byte[] gasLimit = BigInteger.valueOf(10000125000L).toByteArray();

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
        byte[] gasLimitBlock = BigInteger.valueOf(12500000L).toByteArray();
        byte[] balance = BigInteger.valueOf(1_000_000_000_000_000_000L).multiply(BigInteger.valueOf(125L)).toByteArray();

        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(transactionHash, isContractCreation,
                contractAddress, senderAddress, gasPrice, gasLimit, value, data, systemProperties,
                blockchainConfig, parentHash, coinbase, timestamp, number, difficulty, gasLimitBlock, balance);

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

            /**
             * get hash
             *
             * @return hash
             */
            @Override
            public String getHash() {
                return null;
            }
        }

        Repository transactionRepository = new RepositoryImplTest();

        executor = new Executor(transactionRepository, transactionRepository.startTracking(), executionEnvironment);
    }

    @Test
    public void testExecute() {
        ExecutionResult executionResult = executor.execute();

        // 45 is used for ops fee.
        Assert.assertEquals(45, executionResult.getGasUsed().intValue());
        //constructor has no payable modifier, check fails.
        Assert.assertEquals("REVERT opcode executed.", executionResult.getErrorMessage());
        Assert.assertEquals(124955, executionResult.getRemainGas().intValue());
    }

    @Test
    public void testExecute_Event01() {
//        pragma solidity ^0.4.11;
//
//        contract DataStorage {
//            event Sent(address from, uint amount);
//
//            constructor() public {
//                emit Sent(msg.sender, 25);
//            }
//        }
        byte[] data = Hex.decode("6080604052348015600f57600080fd5b50604080513381526019602082015281517f510ffb4dcab972a" +
                "e9d2007a58e13f1b0881776d23cd8f5cc32f8c5be2dbf70d2929181900390910190a160358060586000396000f3fe6080604" +
                "052600080fdfea165627a7a72305820bc81014d71063049e3c08c508a3b7d9858064985493544b42a014cf092b798750029");
        byte[] value = BigInteger.valueOf(0L).toByteArray();
        byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");
        byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
        setExecutor(data, value, contractAddress, senderAddress);


        ExecutionResult executionResult = executor.execute();


        System.out.println("executionResult:");
        System.out.println(executionResult);
        System.out.println();

        Assert.assertEquals(1, executionResult.getLogInfoList().size());
        Assert.assertEquals(Hex.toHexString(contractAddress), Hex.toHexString(executionResult.getLogInfoList().get(0).getAddress()));
        Assert.assertEquals("00000000000000000000000026004361060485763ffffffff7c0100000000000" +
                        "0000000000000000000000000000000000000000000000000000000000000019",
                Hex.toHexString(executionResult.getLogInfoList().get(0).getData()));
        Assert.assertEquals(1, executionResult.getLogInfoList().get(0).getTopics().size());

        System.out.println("topics[0]:");
        //510ffb4dcab972ae9d2007a58e13f1b0881776d23cd8f5cc32f8c5be2dbf70d2
        System.out.println(Hex.toHexString(executionResult.getLogInfoList().get(0).getTopics().get(0).getData()));
        System.out.println();

        Assert.assertTrue(executor.getContractRepository().isExist(contractAddress));
    }

    @Test
    public void testExecute_Event02() {
//        pragma solidity ^0.4.11;
//
//        contract DataStorage {
//            event Sent(uint from, uint amount);
//
//            constructor() public {
//                emit Sent(26, 25);
//            }
//        }
        byte[] data = Hex.decode("6080604052348015600f57600080fd5b5060408051601a81526019602082015281517f233093300525c" +
                "6ff5b81826dc17bf171773b9153d519576d21ee1e2989c1011f929181900390910190a160358060596000396000f3fe60806" +
                "04052600080fdfea165627a7a723058202301acc2cdd1531510b64abffbfdf40a716d53c2d8bfe86631102c7b74194520002" +
                "9");
        byte[] value = BigInteger.valueOf(0L).toByteArray();
        byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");
        byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
        setExecutor(data, value, contractAddress, senderAddress);


        ExecutionResult executionResult = executor.execute();


        System.out.println("executionResult:");
        System.out.println(executionResult);
        System.out.println();

        Assert.assertEquals(1, executionResult.getLogInfoList().size());
        Assert.assertEquals(Hex.toHexString(contractAddress), Hex.toHexString(executionResult.getLogInfoList().get(0).getAddress()));
        Assert.assertEquals("000000000000000000000000000000000000000000000000000000000000001a" +
                        "0000000000000000000000000000000000000000000000000000000000000019",
                Hex.toHexString(executionResult.getLogInfoList().get(0).getData()));
        Assert.assertEquals(1, executionResult.getLogInfoList().get(0).getTopics().size());

        System.out.println("topics[0]:");
        //233093300525c6ff5b81826dc17bf171773b9153d519576d21ee1e2989c1011f
        System.out.println(Hex.toHexString(executionResult.getLogInfoList().get(0).getTopics().get(0).getData()));
        System.out.println();
    }

    @Test
    public void testExecute_Event03() {
//        pragma solidity ^0.4.11;
//
//        contract DataStorage {
//            event Sent(address from, uint s, uint amount);
//
//            constructor() public {
//                emit Sent(msg.sender, 26, 25);
//            }
//        }
        byte[] data = Hex.decode("6080604052348015600f57600080fd5b5060408051338152601a602082015260198183015290517f635" +
                "6739d963da01dc3533acba7203430fcc14f2175d48a8dd0973d7db49c785e9181900360600190a1603580605d6000396000f" +
                "3fe6080604052600080fdfea165627a7a72305820411d5020ee0dae74120beea64fbc24faa9ecb10ed44be9f43b17b4e1f22" +
                "cc0280029");
        byte[] value = BigInteger.valueOf(0L).toByteArray();
        byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");
        byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
        setExecutor(data, value, contractAddress, senderAddress);


        ExecutionResult executionResult = executor.execute();


        System.out.println("executionResult:");
        System.out.println(executionResult);
        System.out.println();

        Assert.assertEquals(1, executionResult.getLogInfoList().size());
        Assert.assertEquals(Hex.toHexString(contractAddress), Hex.toHexString(executionResult.getLogInfoList().get(0).getAddress()));
        Assert.assertEquals("00000000000000000000000026004361060485763ffffffff7c0100000000000" +
                        "000000000000000000000000000000000000000000000000000000000000001a" +
                        "0000000000000000000000000000000000000000000000000000000000000019",
                Hex.toHexString(executionResult.getLogInfoList().get(0).getData()));
        Assert.assertEquals(1, executionResult.getLogInfoList().get(0).getTopics().size());

        System.out.println("topics[0]:");
        //6356739d963da01dc3533acba7203430fcc14f2175d48a8dd0973d7db49c785e
        System.out.println(Hex.toHexString(executionResult.getLogInfoList().get(0).getTopics().get(0).getData()));
        System.out.println();
    }

    @Test
    public void testExecute_Suicide() {
//        pragma solidity ^0.4.11;
//
//        contract DataStorage {
//            constructor() public {
//                selfdestruct(msg.sender);
//            }
//        }
//        PUSH1 0x80 PUSH1 0x40 MSTORE CALLVALUE DUP1 ISZERO PUSH1 0xF JUMPI PUSH1 0x0 DUP1 REVERT JUMPDEST POP CALLER SELFDESTRUCT INVALID
        byte[] data = Hex.decode("6080604052348015600f57600080fd5b5033fffe");
        byte[] value = BigInteger.valueOf(0L).toByteArray();
        byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");
        byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
        setExecutor(data, value, contractAddress, senderAddress);


        ExecutionResult executionResult = executor.execute();


        System.out.println("executionResult:");
        System.out.println(executionResult);
        System.out.println();

        Assert.assertEquals(1, executionResult.getDeleteAccounts().size());
        DataWord account = new DataWord();
        for (DataWord dataWord : executionResult.getDeleteAccounts()) {
            account = dataWord;
        }
        Assert.assertEquals(Hex.toHexString(contractAddress), Hex.toHexString(contractAddress), Hex.toHexString(account.getLast20Bytes()));
        System.out.println();

//        Assert.assertFalse(executor.getContractRepository().isExist(contractAddress));
    }

    @Ignore
    @Test
    public void testExecute_CreateAndCall() {
//        pragma solidity ^0.4.11;
//
//        contract Math {
//            function safeAdd(uint256 _x, uint256 _y) public view returns (uint256) {
//            return  _x + _y;
//            }
//        }
//
//        contract DataStorage {
//            event Add(uint256 result);
//
//            constructor() public {
//                uint256 result = new Math().safeAdd(3, 5);
//                emit Add(result);
//            }
//        }
        byte[] data = Hex.decode("608060405234801561001057600080fd5b50600061001b61012a565b604051809103906000f08015801" +
                "5610037573d6000803e3d6000fd5b5073ffffffffffffffffffffffffffffffffffffffff1663e6cb9013600360056040518" +
                "363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808381526020018" +
                "2815260200192505050602060405180830381600087803b1580156100b057600080fd5b505af11580156100c4573d6000803" +
                "e3d6000fd5b505050506040513d60208110156100da57600080fd5b810190808051906020019092919050505090507f90f1f" +
                "758f0e2b40929b1fd48df7ebe10afc272a362e1f0d63a90b8b4715d799f81604051808281526020019150506040518091039" +
                "0a150610139565b60405160e48061017c83390190565b6035806101476000396000f3006080604052600080fd00a165627a7" +
                "a7230582076f41c44e043cb9ff180649a250f2a859c94484bc2c5b3b2d131afb3dd3f6b68002960806040523480156100105" +
                "7600080fd5b5060c58061001f6000396000f300608060405260043610603f576000357c01000000000000000000000000000" +
                "00000000000000000000000000000900463ffffffff168063e6cb9013146044575b600080fd5b348015604f57600080fd5b5" +
                "060766004803603810190808035906020019092919080359060200190929190505050608c565b60405180828152602001915" +
                "05060405180910390f35b60008183019050929150505600a165627a7a7230582047699b26d403c90fd64109b16d39300047f" +
                "185da132723cb4100bc1a10975d4c0029");
        byte[] value = BigInteger.valueOf(0L).toByteArray();
        byte[] contractAddress = Hex.decode("534b428a1277652677b6adff2d1f3381bbc4115c");
        byte[] senderAddress = Hex.decode("26004361060485763ffffffff7c0100000000000");
        setExecutor(data, value, contractAddress, senderAddress);


        ExecutionResult executionResult = executor.execute();


        System.out.println("executionResult:");
        System.out.println(executionResult);
        System.out.println();

        Assert.assertEquals(1, executionResult.getDeleteAccounts().size());
        DataWord account = new DataWord();
        for (DataWord dataWord : executionResult.getDeleteAccounts()) {
            account = dataWord;
        }
        Assert.assertEquals(Hex.toHexString(contractAddress), Hex.toHexString(contractAddress), Hex.toHexString(account.getLast20Bytes()));
        System.out.println();

//        Assert.assertFalse(executor.getContractRepository().isExist(contractAddress));
    }

    @After
    public void tearDown() {
        executor = null;
    }
}
