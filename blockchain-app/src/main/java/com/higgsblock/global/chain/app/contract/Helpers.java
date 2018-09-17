package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionInput;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutPoint;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.vm.core.AccountDetail;
import com.higgsblock.global.chain.vm.core.AccountState;
import com.higgsblock.global.chain.vm.program.InternalTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tangkun
 * @date 2018-09-14
 */
public class Helpers {

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;



    /**
     * 生成合约执行后的交易 暂不包含退gas
     * @param chainUTXO
     * @param accountState
     * @return
     */
    public static  ContractTransaction buildContractTransaction(List<UTXO> chainUTXO, AccountState accountState,
                                                        List<AccountDetail> accountDetails){
        //余额模型转交易input和output，合约utxo需要压缩，所以utxo都需要作为输入
        ContractTransaction ctx = new ContractTransaction();
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
        txOut.setMoney(new Money(accountState.getBalance().intValue(),accountState.getCurrency()));
        LockScript lockScript = new LockScript();
        lockScript.setAddress(new String(accountState.getCodeHash()));
        txOut.setLockScript(lockScript);

        ctx.setInputs(inputs);
        ctx.setOutputs(outputs);
        ctx.setLockTime(0L);
        ctx.setTransactionTime(System.currentTimeMillis());
        ctx.setVersion((short) 1);

        return  ctx;
    }

    /**
     *
     * @param utxoList
     * @return
     */
    public static BigInteger convertBalance(List<UTXO> utxoList){

        Money balance = new Money();
        for (UTXO utxo:utxoList) {
            balance.add(utxo.getOutput().getMoney());
        }

        return  BalanceUtil.convertMoneyToGas(balance);
    }


}
