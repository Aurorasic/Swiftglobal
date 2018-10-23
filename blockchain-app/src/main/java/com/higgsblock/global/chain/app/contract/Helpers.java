package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.utils.AddrUtil;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.vm.core.AccountDetail;
import com.higgsblock.global.chain.vm.core.AccountState;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tangkun
 * @date 2018-09-14
 */
public class Helpers {

    /**
     * 生成合约执行后的交易 暂不包含退gas
     *
     * @param chainUTXO
     * @param accountState
     * @return
     */
    public static Transaction buildContractTransaction(Set<UTXO> chainUTXO, AccountState accountState,
                                                       List<AccountDetail> accountDetails,
                                                       Money refundCas,
                                                       Transaction transaction) {

        Transaction ctx = new Transaction();
        List<TransactionInput> inputs = new ArrayList<>();

        //merge contract all utxo
        for (UTXO utxo : chainUTXO) {
            TransactionInput input = new TransactionInput();
            TransactionOutPoint preOut = new TransactionOutPoint();
            preOut.setTransactionHash(utxo.getHash());
            preOut.setIndex(utxo.getIndex());
            preOut.setOutput(utxo.getOutput());
            input.setPrevOut(preOut);
            inputs.add(input);
        }

        //transfer log convert transaction outputs
        List<TransactionOutput> outputs = new ArrayList<>();
        for (AccountDetail ad : accountDetails) {
            TransactionOutput txOut = new TransactionOutput();
            txOut.setMoney(BalanceUtil.convertGasToMoney(ad.getValue(), ad.getCurrency()));
            LockScript lockScript = new LockScript();
            lockScript.setAddress(AddrUtil.toTransactionAddr(ad.getTo()));
            txOut.setLockScript(lockScript);
            lockScript.setType(ScriptTypeEnum.P2PKH.getType());
            outputs.add(txOut);
        }

        //contract balance convert outputs
        if (accountState.getBalance().compareTo(BigInteger.ZERO) > 0) {
            TransactionOutput giveChangeOut = new TransactionOutput();
            giveChangeOut.setMoney(BalanceUtil.convertGasToMoney(accountState.getBalance(), accountState.getCurrency()));
            LockScript lockScript = new LockScript();
            lockScript.setAddress(transaction.getOutputs().get(0).getLockScript().getAddress());
            giveChangeOut.setLockScript(lockScript);
            outputs.add(giveChangeOut);
        }
        //TODO tangKun refunds have to sub transaction fee  2018-10-23
        //refund gas convert outputs
        if (refundCas.compareTo(new Money("0")) > 0) {
            LockScript refundLockScript = new LockScript();
            refundLockScript.setAddress(transaction.getInputs().get(0).getPrevOut().getAddress());
            refundLockScript.setType(ScriptTypeEnum.P2PKH.getType());
            TransactionOutput refundCasOut = new TransactionOutput();
            refundCasOut.setMoney(refundCas);
            refundCasOut.setLockScript(refundLockScript);
            outputs.add(refundCasOut);
        }

        ctx.setInputs(inputs);
        ctx.setOutputs(outputs);
        ctx.setLockTime(transaction.getLockTime());
        ctx.setTransactionTime(transaction.getTransactionTime());
        ctx.setVersion(transaction.getVersion());
        ctx.setSubTransaction(true);
        return ctx;
    }

    /**
     * @param utxoList
     * @return
     */
    public static BigInteger convertBalance(List<UTXO> utxoList) {

        Money balance = new Money();
        for (UTXO utxo : utxoList) {
            balance.add(utxo.getOutput().getMoney());
        }

        return BalanceUtil.convertMoneyToGas(balance);
    }

    public static Set<UTXO> buildTestUTXO(String address) {

        return new HashSet() {{
            add(buildUTXO(address, "534b428a1277652677b6adff2d1f3381bbc4115c", "100", "cas"));
            add(buildUTXO(address, "26004361060485763ffffffff7c0100000000000", "10", "cas"));
        }};


    }

    public static UTXO buildUTXO(String address, String hash, String amount, String currency) {
        UTXO utxo = new UTXO();
        utxo.setAddress(address);
        utxo.setHash(hash);
        utxo.setIndex((short) 1);
        TransactionOutput txOut = new TransactionOutput();
        Money money = new Money(amount, currency);
        txOut.setMoney(money);
        LockScript ls = new LockScript();
        ls.setAddress(address);
        txOut.setLockScript(ls);
        utxo.setOutput(txOut);
        return utxo;
    }
}
