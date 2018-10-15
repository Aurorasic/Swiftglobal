package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.IUTXORepository;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.common.utils.Money;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-10-12
 */
public class BestUTXOServiceTest extends BaseMockTest {
    @InjectMocks
    private BestUTXOService bestUTXOService;

    @Mock
    private IUTXORepository utxoRepository;

    @Test
    public void saveUTXO() {
        //save utxo
        UTXO utxo = new UTXO();
        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setMoney(new Money(0, "cas"));
        transactionOutput.setLockScript(new LockScript());
        utxo.setOutput(transactionOutput);
        bestUTXOService.saveUTXO(utxo);
    }

    @Test
    public void getUTXOByKey() {
        String utxoKey = "transactionHash_2";
        //when findByTransactionHashAndOutIndex return null
        PowerMockito.when(utxoRepository.findByTransactionHashAndOutIndex(Mockito.anyString(), Mockito.anyShort())).thenReturn(null);
        Assert.assertNull(bestUTXOService.getUTXOByKey(utxoKey));

        //when the result is queried and returned correctly
        UTXOEntity entity = new UTXOEntity();
        entity.setAmount("2");
        entity.setCurrency("currency");
        PowerMockito.when(utxoRepository.findByTransactionHashAndOutIndex(Mockito.anyString(), Mockito.anyShort())).thenReturn(entity);
        Assert.assertEquals(bestUTXOService.getUTXOByKey(utxoKey).getHash(), entity.getTransactionHash());

    }

    @Test
    public void findByLockScriptAndCurrency() {
        String lockScript = "lockScript",
                currency = "currency";
        bestUTXOService.findByLockScriptAndCurrency(lockScript, currency);
    }

    @Test
    public void getUTXOsByAddress() {
        //when address is null
        String address = null;
        try {
            bestUTXOService.getUTXOsByAddress(address);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertTrue(e.getMessage().contains("address is empty for getUTXOsByAddress"));
        }

        //when address is not null and the list of results queried is empty
        address = "address";
        List<UTXOEntity> utxoEntities = Lists.newArrayList();
        PowerMockito.when(utxoRepository.findByLockScript(address)).thenReturn(utxoEntities);
        Assert.assertTrue(bestUTXOService.getUTXOsByAddress(address).isEmpty());

        //when address is not null and the list of results queried is not empty
        for (int i = 0; i < 3; i++) {
            UTXOEntity entity = new UTXOEntity();
            entity.setAmount(i + "");
            entity.setCurrency("currency" + i);
            utxoEntities.add(entity);
        }
        Assert.assertFalse(bestUTXOService.getUTXOsByAddress(address).isEmpty());
    }

    @Test
    public void deleteByTransactionHashAndOutIndex() throws Exception {
        String transactionHash = "transactionHash";
        short outIndex = 1;
        bestUTXOService.deleteByTransactionHashAndOutIndex(transactionHash, outIndex);
    }
}