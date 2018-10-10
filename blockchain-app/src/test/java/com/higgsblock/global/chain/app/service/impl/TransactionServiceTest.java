package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.Rewards;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.blockchain.transaction.handler.TransactionHandlerTest;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.service.IBalanceService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.app.utils.GetTransactionTestObj;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.crypto.ECKey;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author Su Jiulong
 * @date 2018/9/26
 */
@PrepareForTest({TransactionService.class, ECKey.class})
public class TransactionServiceTest extends BaseMockTest {

    @InjectMocks
    private TransactionService transactionService;

    @InjectMocks
    @Spy
    private TransactionService spyTxService = new TransactionService();

    @Mock
    private TransactionCacheManager txCacheManager;

    @Mock
    private MessageCenter messageCenter;

    @Mock
    private IBalanceService balanceService;

    @Mock
    private BlockService blockService;

    @Mock
    private BlockIndexService blockIndexService;

    @Mock
    private TransactionFeeService transactionFeeService;

    @Mock
    private UTXOServiceProxy utxoServiceProxy;

    @Mock
    private IWitnessService witnessService;

    @Mock
    private TransactionIndexService transactionIndexService;

    @Test
    public void validTransactions() {
        Block block = new Block();
        //transactions is null
        Assert.assertFalse(transactionService.validTransactions(block));
        //transaction list is empty
        List<Transaction> transactions = new ArrayList<>(3);
        block.setTransactions(transactions);
        Assert.assertFalse(transactionService.validTransactions(block));

        //transaction size less than 2 && the transaction is coinBase transaction
        Transaction transaction = new Transaction();
        transactions.add(transaction);
        Assert.assertFalse(transactionService.validTransactions(block));

        //transaction size more than 1
        for (int i = 0; i < 2; i++) {
            Transaction tx = new Transaction();
            transactions.add(tx);
        }
        //coinBase transaction invalid
        Mockito.doReturn(false).when(spyTxService).verifyCoinBaseTx(transactions.get(0), block);
        Assert.assertFalse(spyTxService.validTransactions(block));

        //coinBase transaction valid success
        Mockito.doReturn(true).when(spyTxService).verifyCoinBaseTx(transactions.get(0), block);
        Mockito.doReturn(true).when(spyTxService).verifyTransaction(transactions.get(1), block);
        Mockito.doReturn(false).when(spyTxService).verifyTransaction(transactions.get(2), block);
        //the second transaction valid success but third transaction invalid
        Assert.assertFalse(spyTxService.validTransactions(block));

        //all transactions valid success
        Mockito.doReturn(true).when(spyTxService).verifyTransaction(any(Transaction.class), any(Block.class));
        Assert.assertTrue(spyTxService.validTransactions(block));
    }

    @Test
    public void receivedTransaction() {
        Transaction transaction = new Transaction();
        //the transaction is cached
        PowerMockito.when(txCacheManager.isContains(transaction.getHash())).thenReturn(true);
        spyTxService.receivedTransaction(transaction);

        //the transaction is not cached
        PowerMockito.when(txCacheManager.isContains(transaction.getHash())).thenReturn(false);
        //the transaction valid success
        Mockito.doReturn(false).when(spyTxService).verifyTransaction(transaction, null);
        spyTxService.receivedTransaction(transaction);

        //the transaction invalid
        Mockito.doReturn(true).when(spyTxService).verifyTransaction(transaction, null);
        PowerMockito.doNothing().when(txCacheManager).addTransaction(transaction);
        //received transaction if validate success and board
        PowerMockito.when(messageCenter.broadcast(transaction)).thenReturn(true);
        spyTxService.receivedTransaction(transaction);
    }

    @Test
    public void hasStakeOnBest() {
        SystemCurrencyEnum CAS = SystemCurrencyEnum.CAS;
        String cas = CAS.getCurrency();
        Money balanceMoney = new Money(10, cas);
        String address = "address";
        PowerMockito.when(balanceService.getBalanceOnBest(address, cas)).thenReturn(balanceMoney);
        //the address and currency has stake on best chain
        Assert.assertTrue(transactionService.hasStakeOnBest(address, CAS));

        balanceMoney.setValue("0.5");
        //the address and currency has not stake on best chain
        Assert.assertFalse(transactionService.hasStakeOnBest(address, CAS));
    }

    @Test
    public void hasStake() {
        SystemCurrencyEnum CAS = SystemCurrencyEnum.CAS;
        String cas = CAS.getCurrency();
        Money balanceMoney = new Money(10, cas);
        String address = "address";
        String preBlockHash = "preBlockHash";
        PowerMockito.when(balanceService.getUnionBalance(preBlockHash, address, cas)).thenReturn(balanceMoney);
        //the address and currency has stake
        Assert.assertTrue(transactionService.hasStake(preBlockHash, address, CAS));

        balanceMoney.setValue("0.5");
        //the address and currency has not stake
        Assert.assertFalse(transactionService.hasStake(preBlockHash, address, CAS));
    }

    @Test
    public void getRemovedMiners() {
        Transaction transaction = new Transaction();
        List<TransactionInput> inputList = new ArrayList<>(2);
        transaction.setInputs(inputList);
        Set<String> removeMiners = transactionService.getRemovedMiners(transaction);
        Assert.assertTrue(CollectionUtils.isEmpty(removeMiners));

        TransactionInput transactionInput = null;
        String address = "address";
        for (int i = 0; i < 5; i++) {
            //The first two outputs are null
            transactionInput = new TransactionInput();
            TransactionOutPoint outPoint = new TransactionOutPoint();
            outPoint.setIndex((short) i);
            outPoint.setTransactionHash("preTxHash_" + i);

            if (i >= 2) {
                TransactionOutput preOutput = new TransactionOutput();
                preOutput.setMoney(new Money(1L));
                outPoint.setOutput(preOutput);
                if (i >= 3) {
                    preOutput.getMoney().setCurrency(SystemCurrencyEnum.MINER.getCurrency());
                    LockScript lockScript = new LockScript();
                    lockScript.setAddress(address);
                    preOutput.setLockScript(lockScript);
                }
                outPoint.setOutput(preOutput);
            }
            transactionInput.setPrevOut(outPoint);
            inputList.add(transactionInput);
        }
        //transaction index entity
        TransactionIndexEntity entity = new TransactionIndexEntity();
        PowerMockito.when(transactionIndexService.findByTransactionHash(anyString())).thenReturn(null).thenReturn(entity);

        Mockito.doReturn(false).when(spyTxService).hasStakeOnBest(address, SystemCurrencyEnum.MINER);
        removeMiners = spyTxService.getRemovedMiners(transaction);
        Assert.assertTrue(CollectionUtils.isNotEmpty(removeMiners));
    }

    @Test
    public void getAddedMiners() {
        Transaction transaction = new Transaction();
        List<TransactionOutput> outputs = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            TransactionOutput output = new TransactionOutput();
            outputs.add(output);
            if (i == 4) {
                output.setMoney(new Money(1, SystemCurrencyEnum.CAS.getCurrency()));
                continue;
            }
            output.setMoney(new Money(1, SystemCurrencyEnum.MINER.getCurrency()));
        }
        transaction.setOutputs(outputs);

        UTXO utxo1 = new UTXO();
        String address1 = "address1";
        utxo1.setAddress(address1);

        UTXO utxo2 = new UTXO();
        String address2 = "address2";
        utxo2.setAddress(address2);
        PowerMockito.when(utxoServiceProxy.getUTXOOnBestChain(anyString()))
                .thenReturn(null)
                .thenReturn(utxo1)
                .thenReturn(utxo1)
                .thenReturn(utxo2);
        Mockito.doReturn(true).when(spyTxService).hasStakeOnBest(address1, SystemCurrencyEnum.MINER);
        Mockito.doReturn(false).when(spyTxService).hasStakeOnBest(address2, SystemCurrencyEnum.MINER);
        Set<String> removeMiners = spyTxService.getAddedMiners(transaction);

        Assert.assertTrue(CollectionUtils.isNotEmpty(removeMiners) && (removeMiners.size() == 1));
        for (String miner : removeMiners) {
            Assert.assertEquals(address1, miner);
        }
    }

    @Test
    public void verifyTransaction() throws Exception {
        //build transaction
        Transaction transaction = GetTransactionTestObj.getSingleTransaction();

        TransactionInput input = transaction.getInputs().get(0);
        List<TransactionInput> inputList = transaction.getInputs();
        inputList.add(input);
        //preOutput is null
        Assert.assertFalse(transactionService.verifyTransaction(transaction, null));

        //the input has been spend in this transaction
        UTXO utxo = new UTXO();
        TransactionOutPoint outPoint = inputList.get(0).getPrevOut();
        utxo.setOutput(outPoint.getOutput());
        PowerMockito.when(utxoServiceProxy.getUnionUTXO(null, outPoint.getKey())).thenReturn(utxo);
        Assert.assertFalse(transactionService.verifyTransaction(transaction, null));

        inputList.remove(1);
        TransactionInput input2 = GetTransactionTestObj.buildTxInput(new Money(3));
        inputList.add(input2);
        input2.getPrevOut().setIndex((short) 1);
        UTXO utxo2 = new UTXO();
        TransactionOutPoint outPoint2 = inputList.get(1).getPrevOut();
        utxo2.setOutput(outPoint2.getOutput());
        PowerMockito.when(utxoServiceProxy.getUnionUTXO(null, outPoint2.getKey())).thenReturn(utxo2);

        //The number of input currency types is not equal to the number of output currency types.
        TransactionOutput output = new TransactionOutput();
        output.setMoney(new Money(1L, SystemCurrencyEnum.MINER.getCurrency()));
        LockScript lockScript = new LockScript();
        lockScript.setAddress("miner_address");
        lockScript.setType((short) 0);
        output.setLockScript(lockScript);
        transaction.getOutputs().add(output);
        PowerMockito.when(transactionFeeService.getCurrencyFee(transaction)).thenReturn(new Money("0.5"));
        Assert.assertFalse(transactionService.verifyTransaction(transaction, null));

        //Pre-output currency is null
        inputList.get(1).getPrevOut().getOutput().getMoney().setCurrency(SystemCurrencyEnum.GUARDER.getCurrency());
        Assert.assertFalse(transactionService.verifyTransaction(transaction, null));

        //Not enough fees
        inputList.get(1).getPrevOut().getOutput().getMoney().setCurrency(SystemCurrencyEnum.CAS.getCurrency());
        output.getMoney().setValue("10");
        output.getMoney().setCurrency(SystemCurrencyEnum.CAS.getCurrency());
        Assert.assertFalse(transactionService.verifyTransaction(transaction, null));

        //signature invalid
        transaction.getOutputs().remove(2);
        PowerMockito.mockStatic(ECKey.class);
        PowerMockito.when(ECKey.verifySign(anyString(), anyString(), anyString())).thenReturn(true).thenReturn(false);
        Assert.assertFalse(transactionService.verifyTransaction(transaction, null));

        PowerMockito.when(ECKey.verifySign(anyString(), anyString(), anyString())).thenReturn(true);
        Assert.assertTrue(transactionService.verifyTransaction(transaction, null));
    }

    @Test
    public void verifyTransactionInBlock() throws Exception {
        Block block = new Block();
        //transaction is null
        Transaction transaction = null;
        Assert.assertFalse(transactionService.verifyTransaction(transaction, block));
        //transaction base info invalid
        transaction = new Transaction();
        Assert.assertFalse(transactionService.verifyTransaction(transaction, block));

        //extra invalid
        transaction = GetTransactionTestObj.getSingleTransaction();
        TransactionHandlerTest.assembleExtra(transaction);
        Assert.assertFalse(transactionService.verifyTransaction(transaction, block));

        transaction.setExtra(null);
        String preBlockHash = "preBlockHash";
        block.setPrevBlockHash(preBlockHash);
        List<Transaction> transactions = new ArrayList<>(1);
        transactions.add(transaction);
        block.setTransactions(transactions);

        UTXO utxo = new UTXO();
        TransactionOutPoint outPoint = transaction.getInputs().get(0).getPrevOut();
        utxo.setOutput(outPoint.getOutput());
        PowerMockito.when(utxoServiceProxy.getUnionUTXO(preBlockHash, outPoint.getKey())).thenReturn(utxo);
        PowerMockito.mockStatic(ECKey.class);
        PowerMockito.when(ECKey.verifySign(anyString(), anyString(), anyString())).thenReturn(true);
        Assert.assertTrue(transactionService.verifyTransaction(transaction, block));
    }

    @Test
    public void verifyCoinBaseTxSuccess() throws Exception {
        Block block = new Block();
        block.setHeight(4L);
        List<Transaction> transactions = new ArrayList<>(1);
        //build coinBase transaction
        Transaction coinBaseTx = buildCoinBaseTx();
        transactions.add(coinBaseTx);
        block.setTransactions(transactions);
        String preBlockHash = "preBlockHash";
        block.setPrevBlockHash(preBlockHash);
        Block preBlock = new Block();
        preBlock.setHash(preBlockHash);
        long preBlockHeight = 3L;
        preBlock.setHeight(preBlockHeight);
        PowerMockito.when(blockService.getBlockByHash(preBlockHash)).thenReturn(preBlock);
        Mockito.doReturn(true).when(spyTxService).validPreBlock(preBlock, block.getHeight());
        SortResult sortResult = new SortResult(true, new HashMap<String, Money>());
        Rewards rewards = new Rewards();

        PowerMockito.when(transactionFeeService.orderTransaction(preBlockHash,
                block.getTransactions().subList(1, block.getTransactions().size()))).thenReturn(sortResult);
        PowerMockito.when(transactionFeeService.
                countMinerAndWitnessRewards(sortResult.getFeeMap(), block.getHeight())).thenReturn(rewards);
        PowerMockito.when(transactionFeeService.
                checkCoinBaseMoney(coinBaseTx, rewards.getTotalMoney())).thenReturn(true);

        List<TransactionOutput> outputs = coinBaseTx.getOutputs();
        Mockito.doReturn(true).when(spyTxService).validateProducerOutput(outputs.get(0), rewards.getMinerTotal());
        rewards.setTopTenSingleWitnessMoney(new Money(1));
        rewards.setLastWitnessMoney(new Money(2));
        rewards.setMinerTotal(new Money(3));
        Mockito.doReturn(true).when(spyTxService).
                validateWitnessOutput(outputs.subList(1, outputs.size()), rewards, block.getHeight());
        PowerMockito.when(witnessService.getWitnessSize()).thenReturn(11);
        Assert.assertTrue(spyTxService.verifyCoinBaseTx(coinBaseTx, block));
    }

    @Test
    public void invalidCoinBase() {
        Transaction coinBaseTx = new Transaction();
        List<Transaction> transactions = new ArrayList<>(1);
        transactions.add(coinBaseTx);
        //has inputs
        List<TransactionInput> inputs = new ArrayList<>(1);
        inputs.add(new TransactionInput());
        coinBaseTx.setInputs(inputs);
        Block block = new Block();
        Assert.assertFalse(transactionService.verifyCoinBaseTx(coinBaseTx, block));

        //no inputs and outputs
        coinBaseTx.setInputs(null);
        Assert.assertFalse(transactionService.verifyCoinBaseTx(coinBaseTx, block));

        //The size of outputs does not equal 12
        List<TransactionOutput> outputs = new ArrayList<>(1);
        outputs.add(new TransactionOutput());
        coinBaseTx.setOutputs(outputs);
        Assert.assertFalse(transactionService.verifyCoinBaseTx(coinBaseTx, block));

        //The outputs are equal to 12 but the preblock is null
        coinBaseTx = buildCoinBaseTx();
        block.setHeight(4L);
        String preBlockHash = "preBlockHash";
        block.setPrevBlockHash(preBlockHash);
        PowerMockito.when(blockService.getBlockByHash(preBlockHash)).thenReturn(null);
        Assert.assertFalse(transactionService.verifyCoinBaseTx(coinBaseTx, block));

        Block preBlock = new Block();
        preBlock.setHash(preBlockHash);
        long preBlockHeight = 3L;
        preBlock.setHeight(preBlockHeight);

        //preblock invalid
        PowerMockito.when(blockService.getBlockByHash(preBlockHash)).thenReturn(preBlock);
        Mockito.doReturn(false).when(spyTxService).validPreBlock(preBlock, block.getHeight());
        Assert.assertFalse(transactionService.verifyCoinBaseTx(coinBaseTx, block));

        //prevlock valid success
        Mockito.doReturn(true).when(spyTxService).validPreBlock(preBlock, block.getHeight());

        SortResult sortResult = new SortResult(true, new HashMap<String, Money>());
        Rewards rewards = new Rewards();
        block.setTransactions(transactions);
        PowerMockito.when(transactionFeeService.orderTransaction(preBlockHash,
                block.getTransactions().subList(1, block.getTransactions().size()))).thenReturn(sortResult);
        PowerMockito.when(transactionFeeService.
                countMinerAndWitnessRewards(sortResult.getFeeMap(), block.getHeight())).thenReturn(rewards);
        //verify miner coin base add witness not == total money totalMoney
        PowerMockito.when(transactionFeeService.
                checkCoinBaseMoney(coinBaseTx, rewards.getTotalMoney())).thenReturn(false);
        Assert.assertFalse(spyTxService.verifyCoinBaseTx(coinBaseTx, block));

        PowerMockito.when(transactionFeeService.
                checkCoinBaseMoney(coinBaseTx, rewards.getTotalMoney())).thenReturn(true);
        outputs = coinBaseTx.getOutputs();
        //verify reward of miner failed
        Mockito.doReturn(false).when(spyTxService).validateProducerOutput(outputs.get(0), rewards.getMinerTotal());
        Assert.assertFalse(spyTxService.verifyCoinBaseTx(coinBaseTx, block));

        //topTenSingleWitnessMoney and getLastWitnessMoney invalid
        Mockito.doReturn(true).when(spyTxService).validateProducerOutput(outputs.get(0), rewards.getMinerTotal());
        rewards.setTopTenSingleWitnessMoney(new Money(-1));
        rewards.setLastWitnessMoney(new Money(-1));
        Assert.assertFalse(spyTxService.verifyCoinBaseTx(coinBaseTx, block));

        //topTenSingleWitnessMoney and getLastWitnessMoney valid success
        rewards.setTopTenSingleWitnessMoney(new Money(1));
        rewards.setLastWitnessMoney(new Money(2));
        rewards.setMinerTotal(new Money(3));
        //Validate witness reward failed
        Mockito.doReturn(false).when(spyTxService).
                validateWitnessOutput(outputs.subList(1, outputs.size()), rewards, block.getHeight());
        Assert.assertFalse(spyTxService.verifyCoinBaseTx(coinBaseTx, block));

        //Validate reward failed
        Mockito.doReturn(true).when(spyTxService).
                validateWitnessOutput(outputs.subList(1, outputs.size()), rewards, block.getHeight());
        PowerMockito.when(witnessService.getWitnessSize()).thenReturn(10);
        Assert.assertFalse(spyTxService.verifyCoinBaseTx(coinBaseTx, block));

    }

    @Test
    public void validPreBlock() {
        long currHeight = 3L;
        //preBlock is null
        Assert.assertFalse(transactionService.validPreBlock(null, currHeight));

        Block preBlock = new Block();
        Assert.assertFalse(transactionService.validPreBlock(preBlock, -1L));

        //(preBlock.getHeight() + 1) != height
        preBlock.setHeight(4L);
        Assert.assertFalse(transactionService.validPreBlock(preBlock, currHeight));

        //blockIndex is null
        String preBlockHash = "preBlockHash";
        long preBlockHeight = currHeight - 1;
        preBlock.setHeight(preBlockHeight);
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(preBlockHeight)).thenReturn(null);
        Assert.assertFalse(transactionService.validPreBlock(preBlock, currHeight));

        //build block index
        BlockIndex blockIndex = new BlockIndex();
        ArrayList<String> blockHashs = new ArrayList<>(4);
        //blockHashs is empty
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(preBlockHeight)).thenReturn(blockIndex);
        Assert.assertFalse(transactionService.validPreBlock(preBlock, currHeight));

        //the blockHashs not contain preBlockHash
        preBlock.setHash(preBlockHash);
        for (int i = 0; i < 3; i++) {
            blockHashs.add("preBlockHash_" + i);
        }
        blockIndex.setBlockHashs(blockHashs);
        Assert.assertFalse(transactionService.validPreBlock(preBlock, currHeight));

        //the blockHashs contain preBlockHash
        blockHashs.add(preBlockHash);
        Assert.assertTrue(transactionService.validPreBlock(preBlock, currHeight));
    }

    @Test
    public void validateProducerOutput() {
        Transaction coinBaseTx = buildCoinBaseTx();
        TransactionOutput output = coinBaseTx.getOutputs().get(0);
        String value = "3";
        Money totalReward = new Money("-1");
        Assert.assertFalse(transactionService.validateProducerOutput(output, totalReward));

        totalReward.setValue(value);
        Assert.assertFalse(transactionService.validateProducerOutput(null, totalReward));

        //lockScript is null
        TransactionOutput faildOutput = new TransactionOutput();
        Assert.assertFalse(transactionService.validateProducerOutput(faildOutput, totalReward));

        //the currency is not CAS
        faildOutput.setLockScript(new LockScript());
        faildOutput.setMoney(new Money(1L, SystemCurrencyEnum.MINER.getCurrency()));
        Assert.assertFalse(transactionService.validateProducerOutput(faildOutput, totalReward));

        //the money value inequality
        faildOutput.getMoney().setValue("1");
        faildOutput.getMoney().setCurrency(SystemCurrencyEnum.CAS.getCurrency());
        Assert.assertFalse(transactionService.validateProducerOutput(faildOutput, totalReward));

        //valid success
        Assert.assertTrue(transactionService.validateProducerOutput(output, totalReward));
    }

    @Test
    public void validateWitnessOutput() {
        long height = 4;
        Rewards rewards = new Rewards();

        //outputs is empty
        List<TransactionOutput> outputs = new ArrayList<>(1);
        Assert.assertFalse(transactionService.validateWitnessOutput(outputs, rewards, height));

        //lockScript is null
        TransactionOutput output = new TransactionOutput();
        outputs.add(output);
        Assert.assertFalse(transactionService.validateWitnessOutput(outputs, rewards, height));

        LockScript lockScript = new LockScript();
        output.setLockScript(lockScript);
        //currency is not cas
        output.setMoney(new Money(1, SystemCurrencyEnum.MINER.getCurrency()));
        Assert.assertFalse(transactionService.validateWitnessOutput(outputs, rewards, height));

        //rewards invalid
        outputs = buildCoinBaseTx().getOutputs();
        rewards = buildWitnessRewards(outputs);
        rewards.setLastWitnessMoney(new Money(1));
        rewards.setTotalMoney(new Money(15));
        rewards.setTopTenSingleWitnessMoney(new Money("1.1"));
        rewards.setMinerTotal(new Money(4));
        Assert.assertFalse(transactionService.validateWitnessOutput(outputs.subList(1, outputs.size()), rewards, height));

        //last witness reward invalid
        rewards.getTopTenSingleWitnessMoney().setValue("1");
        Assert.assertFalse(transactionService.validateWitnessOutput(outputs.subList(1, outputs.size()), rewards, height));

        //TopTenSingleWitness reward invalid
        rewards.getTopTenSingleWitnessMoney().setValue("1.1");
        rewards.getLastWitnessMoney().setValue("2");
        rewards.getMinerTotal().setValue("2");
        Assert.assertFalse(transactionService.validateWitnessOutput(outputs.subList(1, outputs.size()), rewards, height));

        rewards.getTopTenSingleWitnessMoney().setValue("1");
        rewards.getMinerTotal().setValue("3");
        Assert.assertTrue(transactionService.validateWitnessOutput(outputs.subList(1, outputs.size()), rewards, height));
    }

    @Test
    public void broadcastTransaction() {
        Transaction transaction = new Transaction();
        PowerMockito.when(messageCenter.broadcast(transaction)).thenReturn(true);
        transactionService.broadcastTransaction(transaction);
    }

    private Transaction buildCoinBaseTx() {
        Transaction transaction = new Transaction();
        addOutputs(transaction);
        return transaction;
    }

    private void addOutputs(Transaction transaction) {
        List<TransactionOutput> outputs = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            TransactionOutput transactionOutput = new TransactionOutput();
            LockScript lockScript = new LockScript();
            if (i == 0) {
                transactionOutput.setMoney(new Money(3));
                lockScript.setAddress("miner address");
            } else {
                transactionOutput.setMoney(new Money(1));
                lockScript.setAddress("address_" + i);
            }
            //validateWitnessOutput using
            if (i == 5) {
                transactionOutput.getMoney().setValue("2");
            }
            transactionOutput.setLockScript(lockScript);
            outputs.add(transactionOutput);
        }
        transaction.setOutputs(outputs);
    }

    private Rewards buildWitnessRewards(List<TransactionOutput> outputs) {
        Rewards rewards = new Rewards();
        rewards.setLastWitnessMoney(outputs.get(5).getMoney());
        rewards.setTopTenSingleWitnessMoney(outputs.get(1).getMoney());
        rewards.setMinerTotal(outputs.get(0).getMoney());
        rewards.setTotalMoney(new Money("15"));
        return rewards;
    }
}