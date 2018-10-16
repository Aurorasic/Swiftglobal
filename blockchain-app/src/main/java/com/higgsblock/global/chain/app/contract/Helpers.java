package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.utils.AddrUtil;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.vm.core.AccountDetail;
import com.higgsblock.global.chain.vm.core.AccountState;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
    public static ContractTransaction buildContractTransaction(List<UTXO> chainUTXO, AccountState accountState,
                                                               List<AccountDetail> accountDetails) {
        //余额模型转交易input和output，合约utxo需要压缩，所以utxo都需要作为输入
        ContractTransaction ctx = new ContractTransaction();
        List<TransactionInput> inputs = new ArrayList<>();
        for (UTXO utxo : chainUTXO) {
            TransactionInput input = new TransactionInput();

            TransactionOutPoint preOut = new TransactionOutPoint();
            preOut.setTransactionHash(utxo.getHash());
            preOut.setIndex(utxo.getIndex());
            input.setPrevOut(preOut);

            inputs.add(input);
        }

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

        //合约地址找零
        TransactionOutput giveChangeOut = new TransactionOutput();
        giveChangeOut.setMoney(BalanceUtil.convertGasToMoney(accountState.getBalance(), accountState.getCurrency()));
        LockScript lockScript = new LockScript();
        lockScript.setAddress(AddrUtil.toTransactionAddr(accountState.getCodeHash()));
        giveChangeOut.setLockScript(lockScript);
        outputs.add(giveChangeOut);

        ctx.setInputs(inputs);
        ctx.setOutputs(outputs);
        ctx.setLockTime(0L);
        ctx.setTransactionTime(System.currentTimeMillis());
        ctx.setVersion((short) 1);

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

    public static List<UTXO> buildTestUTXO(String address) {

        return new ArrayList() {{
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
