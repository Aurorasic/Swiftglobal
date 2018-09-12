package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionInput;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutPoint;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
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
    //区块级别未花费缓存，交易执行成功花费后会移除到spentUTXOCache中，并新增加新的utxo
    Map<String,List<UTXO>> unspentUTXOCache = new HashMap<>();
    Map<String,List<UTXO>> spentUTXOCache = new HashMap<>();



    @Test
    public void testUTXO(){
        String contractAddress = "oxcc";
        String preBlockHash=null, address=null , currency=null;
        //查询以上一个区块hash为基础的
        List<UTXO> chainUTXO = utxoServiceProxy.getUnionUTXO(preBlockHash,address,currency);

        //获取合约上下文utxo
        List<UTXO> transactionUTXO = new ArrayList<>();
        chainUTXO.addAll(transactionUTXO);
        //合并db+tx的uxto为账户余额模型

        AccountState accountState = new AccountState(BigInteger.ZERO,contractAddress.getBytes());
        accountState = accountState.withBalanceIncrement(convertBalance(chainUTXO));
        accountStates.put(new String(accountState.getCodeHash()),accountState);
        //转给A 11个cas
        if(accountState.getBalance().intValue() < 11){
            //余额不足
        }

        //检查input>=outputs
        InternalTransaction internalTx =  buildInternalTransaction(chainUTXO,accountState);
        //删除使用后的utxo
        unspentUTXOCache.get(contractAddress).removeAll(chainUTXO);
        int outputSize = internalTx.getOutputs().size();
        for (int i = 0; i < outputSize; i++) {
            TransactionOutput output = internalTx.getOutputs().get(i);
            UTXO utxo = new UTXO(internalTx, (short) i, output);
            unspentUTXOCache.get(utxo.getAddress()).add(utxo);
        }

        //是否重复
        if(spentUTXOCache.getOrDefault(contractAddress,new ArrayList<>()).containsAll(chainUTXO)){
            //双花
        }
        spentUTXOCache.getOrDefault(contractAddress,new ArrayList<>()).addAll(chainUTXO);



    }



    /**
     * 生成合约执行后的交易 暂不包含退gas
     * @param chainUTXO
     * @param accountState
     * @return
     */
    public InternalTransaction buildInternalTransaction(List<UTXO> chainUTXO,AccountState accountState){
        //余额模型转交易input和output，合约utxo需要压缩，所以utxo都需要作为输入
        InternalTransaction internalTransaction = new InternalTransaction();
        List<TransactionInput> inputs = new ArrayList<>();
        for(UTXO utxo : chainUTXO){
            TransactionInput input = new TransactionInput();

            TransactionOutPoint preOut = new TransactionOutPoint();
            preOut.setTransactionHash(utxo.getHash());
            preOut.setIndex(utxo.getIndex());
            input.setPrevOut(preOut);

            inputs.add(input);
        }

        List<TransactionOutput> outputs =new ArrayList<>();
        for (AccountDetail ad : accountDetails ){
            TransactionOutput txOut = new TransactionOutput();

            txOut.setMoney(new Money(ad.getValue().intValue(),ad.getCurrency()));
            LockScript lockScript = new LockScript();
            lockScript.setAddress(ad.getTo());
            txOut.setLockScript(lockScript);
            //lockScript.setType();

            outputs.add(txOut);
        }

        //合约地址找零
        TransactionOutput txOut = new TransactionOutput();
        txOut.setMoney(new Money(accountState.getBalance().intValue(),null));
        LockScript lockScript = new LockScript();
        lockScript.setAddress(new String(accountState.getCodeHash()));
        txOut.setLockScript(lockScript);

        internalTransaction.setInputs(inputs);
        internalTransaction.setOutputs(outputs);
        internalTransaction.setLockTime(0L);
        internalTransaction.setTransactionTime(System.currentTimeMillis());
        internalTransaction.setVersion((short) 1);
        internalTransaction.setParentHash(null);
        internalTransaction.setHash(null);

        return  internalTransaction;
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

        AccountDetail accountDetail = new AccountDetail(from,new String(to.getCodeHash()),new BigInteger(amount),to.getBalance(),currency);
        accountDetails.add(accountDetail);
    }




}


