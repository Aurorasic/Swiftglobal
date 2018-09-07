package com.higgsblock.global.chain.app.vm;

import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.vm.core.AccountState;
import org.junit.Test;

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

    Map<String,AccountState> accountStates = new HashMap<>();
    List<AccountDetail> accountDetails = new ArrayList<>();

    @Test
    public void testUTXO(){
        Money money = new Money("11");
        LockScript lockScript = new LockScript();
        lockScript.setAddress("oxcc");
        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setMoney(money);
        transactionOutput.setLockScript(lockScript);
        UTXO utxoFromDb = new UTXO();
        utxoFromDb.setAddress("0xcc");
        utxoFromDb.setOutput(transactionOutput);


        AccountState accountState = new AccountState(new BigInteger("21"),"oxcc".getBytes());
        accountStates.put(new String(accountState.getCodeHash()),accountState);
        //转给A 11个cas
        if(accountState.getBalance().intValue() < 11){
            //余额不足
        }

        AccountState A = accountStates.getOrDefault("A",new AccountState(new BigInteger("0"),"A".getBytes())) ;
        A=A.withBalanceIncrement(new BigInteger("11"));
        accountStates.put(new String(A.getCodeHash()),A);

        AccountDetail accountDetail = new AccountDetail("0xcc",new String(A.getCodeHash()),new BigInteger("11"),A.getBalance());
        accountDetails.add(accountDetail);

        //转给B 9个cas
        if(accountState.getBalance().intValue() < 9){
            //余额不足
        }
        AccountState B = accountStates.getOrDefault("B",new AccountState(new BigInteger("0"),"B".getBytes())) ;
        B=B.withBalanceIncrement(new BigInteger("9"));
        accountStates.put(new String(B.getCodeHash()),B);

        AccountDetail accountDetailB = new AccountDetail("0xcc",new String(B.getCodeHash()),new BigInteger("9"),B.getBalance());
        accountDetails.add(accountDetailB);

        //

    }

}


