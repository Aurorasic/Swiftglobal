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

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    @Autowired
    private Repository repository;



    @Test
    public void testUTXO(){

        Repository repository  = new RepositoryImpl();

        String contractAddress = "oxcc";
        String preBlockHash=null, address="0xaa" , currency="cas";
        //查询以上一个区块hash为基础的
        List<UTXO> chainUTXO = utxoServiceProxy.getUnionUTXO(preBlockHash,address,currency);
        //获取合约上下文utxo
        List<UTXO> transactionUTXO = new ArrayList<>();
        chainUTXO.addAll(transactionUTXO);
        //合并db+tx的uxto为账户余额模型
        BigInteger balance = Helpers.convertBalance(chainUTXO);
        AccountState accountState = repository.createAccountState(address, balance, currency);


        //转给A 11个cas
        if(accountState.getBalance().intValue() < 11){
            //余额不足
            //String from,String address ,String amount,String currency

        }
        repository.transfer(contractAddress,address,"11",currency);

        //检查input>=outputs
        ContractTransaction internalTx =  Helpers.buildContractTransaction(chainUTXO,accountState,repository.getAccountDetails());

        int outputSize = internalTx.getOutputs().size();
        List<UTXO> unSpendUTXO = new ArrayList<>(outputSize);
        for (int i = 0; i < outputSize; i++) {
            TransactionOutput output = internalTx.getOutputs().get(i);
            UTXO utxo = new UTXO(internalTx, (short) i, output);
            unSpendUTXO.add(utxo);
        }

        repository.mergeUTXO(chainUTXO,unSpendUTXO);

        //刷新


        //是否重复
//        if(spentUTXOCache.getOrDefault(contractAddress,new ArrayList<>()).containsAll(chainUTXO)){
//            //双花
//        }
//        spentUTXOCache.getOrDefault(contractAddress,new ArrayList<>()).addAll(chainUTXO);



    }












}


