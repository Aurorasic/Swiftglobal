package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionInput;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutPoint;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.contract.ContractTransaction;
import com.higgsblock.global.chain.app.contract.Helpers;
import com.higgsblock.global.chain.app.contract.RepositoryImpl;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.vm.core.AccountState;
import com.higgsblock.global.chain.vm.core.Repository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.*;

/**
 * @author tangkun
 * @date 2018-09-07
 */
public class SnapshotTest {

    @Test
    public void testUTXO(){


        //一级缓存
        Repository parent  = new RepositoryImpl();
        //二级缓存
        Repository txR = parent.startTracking();
        //三级缓存
        Repository conR = txR.startTracking();
        String from = "oxaa",
         amount="10", currency="cas";
        conR.transfer(from,"oxbb","10",currency);
        conR.transfer(from,"oxcc","110",currency);

        //检查input>=outputs

//        List<UTXO>
//        ContractTransaction internalTx =  Helpers.buildContractTransaction(chainUTXO,accountState,repository.getAccountDetails());
//
//        int outputSize = internalTx.getOutputs().size();
//        List<UTXO> unSpendUTXO = new ArrayList<>(outputSize);
//        for (int i = 0; i < outputSize; i++) {
//            TransactionOutput output = internalTx.getOutputs().get(i);
//            UTXO utxo = new UTXO(internalTx, (short) i, output);
//            unSpendUTXO.add(utxo);
//        }
//
//        repository.mergeUTXO(chainUTXO,unSpendUTXO);

        //刷新


        //是否重复
//        if(spentUTXOCache.getOrDefault(contractAddress,new ArrayList<>()).containsAll(chainUTXO)){
//            //双花
//        }
//        spentUTXOCache.getOrDefault(contractAddress,new ArrayList<>()).addAll(chainUTXO);



    }












}


