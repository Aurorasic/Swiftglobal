package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.utils.GetTransactionTestObj;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;

/**
 * @author Su Jiulong
 * @date 2018/10/11
 */
@PrepareForTest({UTXOServiceProxy.class, Maps.class})
public class UTXOServiceProxyTest extends BaseMockTest {
    @InjectMocks
    private UTXOServiceProxy utxoServiceProxy;

    @InjectMocks
    @Spy
    private UTXOServiceProxy spyUtxoServiceProxy = new UTXOServiceProxy();

    @Mock
    private BlockService blockService;

    @Mock
    private BlockIndexService blockIndexService;

    @Mock
    private BestUTXOService bestUtxoService;

    @Test
    public void getUnconfirmedBalance() {
        String address = "address";
        String currency = SystemCurrencyEnum.CAS.getCurrency();
        try {
            utxoServiceProxy.getUnconfirmedBalance(null, address, currency);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertTrue(e.getMessage().contains("error preBlockHash for getUnconfirmedBalance"));
        }

        //This unconfirmedUtxoMaps does not exist for this hash and the block not exist db
        String preBlockHash = "BlockHeight_2_Hash";
        Money expectedMoeny = new Money(0, currency);
        Money actualMoney = utxoServiceProxy.getUnconfirmedBalance(preBlockHash, address, currency);
        Assert.assertEquals(expectedMoeny, actualMoney);

        //the block exist db but block index not exist of the block
        Block block = new Block();
        block.setHeight(10L);
        PowerMockito.when(blockService.getBlockByHash(preBlockHash)).thenReturn(block);
        actualMoney = utxoServiceProxy.getUnconfirmedBalance(preBlockHash, address, currency);
        Assert.assertEquals(expectedMoeny, actualMoney);

        //block index is not null but the block has confirmed on chain
        BlockIndex blockIndex = new BlockIndex();
        blockIndex.setBestBlockHash(preBlockHash);
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(block.getHeight())).thenReturn(blockIndex);
        actualMoney = utxoServiceProxy.getUnconfirmedBalance(preBlockHash, address, currency);
        Assert.assertEquals(expectedMoeny, actualMoney);

        buildMockBlockIndex(block, blockIndex, address);
        block.setPrevBlockHash("BlockHeight_1_Hash");
        actualMoney = utxoServiceProxy.getUnconfirmedBalance(preBlockHash, address, currency);

        expectedMoeny.setValue("1");
        Assert.assertEquals(expectedMoeny, actualMoney);
    }

    @Test
    public void getUnionUTXOByQuery() throws Exception {
        String address = "address";
        BlockIndex lastBlockIndex = new BlockIndex();
        lastBlockIndex.setBlockHashs(new ArrayList<>(getLastHeightBlockHashs()));
        PowerMockito.when(blockIndexService.getLastBlockIndex()).thenReturn(lastBlockIndex);
        PowerMockito.doNothing().when(spyUtxoServiceProxy, "getUnionUTXOsRecurse", anyMap(), anyString(), anyBoolean());

        //firstBlockHash is null
        lastBlockIndex.getBlockHashs().add(0, null);
        try {
            utxoServiceProxy.getUnionUTXO(null, address, null);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertTrue(e.getMessage().contains("error lastBlockIndex"));
        }
        lastBlockIndex.getBlockHashs().remove(0);

        List<UTXO> utxos = buildUTXOList(address, 4);
        PowerMockito.when(bestUtxoService.getUTXOsByAddress(address)).thenReturn(utxos);

        Map<String, Map<String, UTXO>> unconfirmedUtxoMaps = new HashMap<>(1);
        unconfirmedUtxoMaps.put("transactionHash_1", null);
        PowerMockito.mockStatic(Maps.class);
        PowerMockito.when(Maps.class, "newHashMap").thenReturn(unconfirmedUtxoMaps).thenReturn(new HashMap<>(0));

        //preBlockHash and currency is null
        List<UTXO> result = utxoServiceProxy.getUnionUTXO(null, address, null);
        Assert.assertEquals(3, result.size());
        //preBlockHash is null but currency is cas
        result = utxoServiceProxy.getUnionUTXO(null, address, SystemCurrencyEnum.CAS.getCurrency());
        Assert.assertEquals(2, result.size());

        //preBlockHash is not null but currency is null
        String preBlockHash = "preBlockHash";
        result = utxoServiceProxy.getUnionUTXO(preBlockHash, address, null);
        Assert.assertEquals(4, result.size());

        //preBlockHash is not null but currency is null
        result = utxoServiceProxy.getUnionUTXO(preBlockHash, address, SystemCurrencyEnum.MINER.getCurrency());
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void whenPreBlockHashIsNullgetUnionUTXO() throws Exception {
        List<String> lastHeightBlockHashs = getLastHeightBlockHashs();
        PowerMockito.when(blockIndexService.getLastHeightBlockHashs()).thenReturn(lastHeightBlockHashs);
        String address = "address";
        //preBlockHash is null
        mockGetAndLoadUnconfirmedUtxoMaps(lastHeightBlockHashs, address);
        testMock(null);
    }

    @Test
    public void whenPreBlockHashIsNotNullgetUnionUTXO() throws Exception {
        String preBlockHash = "preBlockHash_2";
        Block preBlock = new Block();
        preBlock.setHash(preBlockHash);
        preBlock.setPrevBlockHash("preBlockHash_1");
        BlockIndex blockIndex = new BlockIndex();
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(anyLong())).thenReturn(blockIndex);
        buildMockBlockIndex(preBlock, blockIndex, "address");
        PowerMockito.when(blockService.getBlockByHash(preBlockHash)).thenReturn(preBlock);
        testMock(preBlockHash);
    }

    @Test
    public void addNewBlock() {
        List<String> blockHashs = getLastHeightBlockHashs();
        final int size = blockHashs.size();
        ArrayList<String> bestBlockHashs = new ArrayList<>(size);
        bestBlockHashs.addAll(blockHashs);
        Block newBestBlock = new Block();

        newBestBlock.setHeight(100L);
        BlockIndex bestBlockIndex = new BlockIndex();
        bestBlockIndex.setBlockHashs(bestBlockHashs);
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(newBestBlock.getHeight())).thenReturn(bestBlockIndex);
        Block newBlock = new Block();
        List<Transaction> transactions = new ArrayList<>(1);
        transactions.add(GetTransactionTestObj.getSingleTransaction());
        newBlock.setTransactions(transactions);

        Map<String, Map<String, UTXO>> unconfirmedUtxoMaps = new HashMap<>(size);
        Map<String, String> blockHashChainMap = new HashMap<>(size);

        for (String blockHash : blockHashs) {
            unconfirmedUtxoMaps.put(blockHash, Collections.emptyMap());
            blockHashChainMap.put(blockHash, "preBlockHash");
        }
        Whitebox.setInternalState(utxoServiceProxy, "unconfirmedUtxoMaps", unconfirmedUtxoMaps);
        Whitebox.setInternalState(utxoServiceProxy, "blockHashChainMap", blockHashChainMap);
        utxoServiceProxy.addNewBlock(newBestBlock, newBlock);
        Assert.assertTrue(unconfirmedUtxoMaps.size() == 1);
        Assert.assertTrue(blockHashChainMap.size() == 1);
    }

    private List<String> getLastHeightBlockHashs() {
        List<String> lastHeightBlockHashs = new ArrayList<>(3);
        for (int i = 3; i > 0; i--) {
            lastHeightBlockHashs.add("lastHeightBlockHashs_" + i);
        }
        return lastHeightBlockHashs;
    }

    private void mockGetAndLoadUnconfirmedUtxoMaps(List<String> preBlockHashs, String address) {
        int size = preBlockHashs.size();
        BlockIndex blockIndex = new BlockIndex();
        blockIndex.setBestBlockHash("preBlockHash");
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(anyLong())).thenReturn(blockIndex);
        for (int i = 0; i < size; i++) {
            Block block = new Block();
            block.setHash(preBlockHashs.get(i));
            if (i != (size - 1)) {
                block.setPrevBlockHash(preBlockHashs.get(i + 1));
            }
            PowerMockito.when(blockService.getBlockByHash(block.getHash())).thenReturn(block);
            //the block has not confirmed on chain
            buildMockBlockIndex(block, blockIndex, address);
        }
    }

    private void buildMockBlockIndex(Block block, BlockIndex blockIndex, String address) {
        //the block has not confirmed on chain
        blockIndex.setBestBlockHash("bestHash");
        List<Transaction> transactions = new ArrayList<>(1);
        Transaction transaction = GetTransactionTestObj.getSingleTransaction();
        transaction.getOutputs().get(0).getLockScript().setAddress(address);
        TransactionOutput output = transaction.getOutputs().get(1);
        output.getLockScript().setAddress(address);
        output.getMoney().setValue("3");
        transactions.add(transaction);
        block.setTransactions(transactions);
    }

    private void testMock(String preBlockHash) throws Exception {
        //This utxo has been spent on the non-main chain
        String utxoKey = "spent tx Hash_0";
        UTXO result = utxoServiceProxy.getUnionUTXO(preBlockHash, utxoKey);
        Assert.assertEquals(null, result);

        //This utxo is in an unconfirmed block
        utxoKey = "This utxo is in an unconfirmed block";
        Map<String, UTXO> utxoMap = new HashMap<>(1);
        UTXO utxo = new UTXO();
        utxoMap.put(utxoKey, utxo);
        PowerMockito.doReturn(utxoMap).when(spyUtxoServiceProxy, "getAndLoadUnconfirmedUtxoMaps", anyString());
        result = spyUtxoServiceProxy.getUnionUTXO(preBlockHash, utxoKey);
        Assert.assertEquals(utxo, result);

        //This utxo is not in confirmed block chain
        utxoKey = "best chain utxo";
        PowerMockito.when(bestUtxoService.getUTXOByKey(anyString())).thenReturn(null);
        result = utxoServiceProxy.getUnionUTXO(preBlockHash, utxoKey);
        Assert.assertEquals(null, result);

        if (preBlockHash == null) {
            //This utxo is in confirmed block chain
            PowerMockito.when(bestUtxoService.getUTXOByKey(anyString())).thenReturn(null).thenReturn(utxo);
        } else {
            //This utxo is in confirmed block chain
            PowerMockito.when(bestUtxoService.getUTXOByKey(anyString())).thenReturn(utxo);
        }
        result = utxoServiceProxy.getUnionUTXO(preBlockHash, utxoKey);
        Assert.assertEquals(utxo, result);
    }

    private List<UTXO> buildUTXOList(String address, final int size) {
        List<UTXO> utxos = new ArrayList<>(size);
        TransactionOutput output = null;
        UTXO utxo = null;
        for (int i = 0; i < size; i++) {
            utxo = new UTXO();
            utxo.setAddress(address);
            utxo.setHash("transactionHash");
            utxo.setIndex((short) i);
            output = new TransactionOutput();
            if (i % 2 == 0) {
                output.setMoney(new Money(1, SystemCurrencyEnum.MINER.getCurrency()));
            } else {
                output.setMoney(new Money(2));
            }
            utxo.setOutput(output);
            utxos.add(utxo);
        }
        return utxos;
    }
}