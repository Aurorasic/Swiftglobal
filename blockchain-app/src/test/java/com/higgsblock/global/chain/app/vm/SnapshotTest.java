package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.vm.core.AccountState;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangkun
 * @date 2018-09-07
 */
public class SnapshotTest {

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    Map<String,AccountState> accountStates = new HashMap<>();
    List<AccountDetail> accountDetails = new ArrayList<>();

    @Test
    public void testUTXO(){

        String preBlockHash=null, address=null , currency=null;
        //查询以上一个区块hash为基础的
        List<UTXO> chainUTXO = utxoServiceProxy.getUnionUTXO(preBlockHash,address,currency);

        //获取合约上下文utxo
        List<UTXO> transactionUTXO = new ArrayList<>();
        chainUTXO.addAll(transactionUTXO);
        //合并db+tx的uxto为账户余额模型


        AccountState accountState = new AccountState(BigInteger.ZERO,"oxcc".getBytes());
        accountState = accountState.withBalanceIncrement(convertBalance(chainUTXO));
        accountStates.put(new String(accountState.getCodeHash()),accountState);
        //转给A 11个cas
        if(accountState.getBalance().intValue() < 11){
            //余额不足
        }

        //余额模型转交易input和output，合约utxo需要压缩，所以utxo都需要作为输入
        InternalTransaction internalTransaction = new 


    }

    //utxo转账户余额
    public BigInteger convertBalance(List<UTXO> utxoList){

        Money balance = new Money();
        for (UTXO utxo:utxoList) {
           balance.add(utxo.getOutput().getMoney());
        }
        //合约使用的单位是否为整数
        return  new BigInteger("21");
    }

    //转账操作
    public void transfer(String from,String address ,String amount,String currency){
        AccountState to = accountStates.getOrDefault(address,new AccountState(BigInteger.ZERO,address.getBytes())) ;
        to=to.withBalanceIncrement(new BigInteger(amount));
        accountStates.put(new String(to.getCodeHash()),to);

        AccountDetail accountDetail = new AccountDetail(from,new String(to.getCodeHash()),new BigInteger(amount),to.getBalance());
        accountDetails.add(accountDetail);
    }

    //按账户余额生成内部交易
//    public Transaction createInternalTransaction(){
//
//    }
}


