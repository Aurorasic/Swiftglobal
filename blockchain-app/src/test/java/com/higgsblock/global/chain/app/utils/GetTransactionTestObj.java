package com.higgsblock.global.chain.app.utils;

import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.script.UnLockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionInput;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutPoint;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.common.utils.Money;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018/9/29
 */
public class GetTransactionTestObj {

    private static volatile Transaction singletonTransaction;

    public static Transaction getSingleTransaction() {
        if (null == singletonTransaction) {
            synchronized (Transaction.class) {
                if (null == singletonTransaction) {
                    singletonTransaction = new Transaction();
                    //set Transaction inputs
                    buildTxInputs(singletonTransaction);
                    //set Transaction outputs
                    buildTxOutputs(singletonTransaction);
                }
            }
        }
        return singletonTransaction;
    }

    private static void buildTxInputs(Transaction transaction) {
        List<TransactionInput> inputList = new ArrayList<>(2);
        inputList.add(buildTxInput(new Money(3)));
        transaction.setInputs(inputList);
    }

    private static void buildTxOutputs(Transaction transaction) {
        List<TransactionOutput> outputs = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            TransactionOutput output = new TransactionOutput();
            output.setMoney(new Money(1L));
            LockScript lockScript = new LockScript();
            lockScript.setAddress("address" + i);
            lockScript.setType((short) i);
            output.setLockScript(lockScript);
            outputs.add(output);
        }
        transaction.setOutputs(outputs);
    }

    public static TransactionInput buildTxInput(Money money) {
        TransactionInput input = new TransactionInput();
        TransactionOutPoint prevOut = new TransactionOutPoint();
        prevOut.setTransactionHash("spent tx Hash");
        prevOut.setIndex((short) 0);
        TransactionOutput preOutput = new TransactionOutput();
        preOutput.setMoney(money);
        LockScript lockScript = new LockScript();
        lockScript.setAddress("address");
        lockScript.setType((short) 1);
        preOutput.setLockScript(lockScript);
        prevOut.setOutput(preOutput);

        UnLockScript unLockScript = new UnLockScript();
        List<String> sigList = new ArrayList<>(2);
        sigList.add("sig");
        List<String> pkList = new ArrayList<>(2);
        pkList.add("pk");
        unLockScript.setPkList(pkList);
        unLockScript.setSigList(sigList);
        input.setUnLockScript(unLockScript);
        input.setPrevOut(prevOut);
        return input;
    }
}
