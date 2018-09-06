package com.higgsblock.global.chain.vm.core;

import java.math.BigInteger;

/**
 * @author tangkun
 * @date 2018-09-06
 */
public class AccountState {

    private final BigInteger balance;

    private final byte[] codeHash;



    public AccountState(BigInteger balance,byte[] codeHash){
        this.balance=balance;
        this.codeHash=codeHash;
    }

    public AccountState withCodeHash(byte[] codeHash) {
        return new AccountState(balance,  codeHash);
    }

    public BigInteger getBalance() {
        return balance;
    }

    public byte[] getCodeHash() {
        return codeHash;
    }

    public AccountState withBalanceIncrement(BigInteger value) {
        return new AccountState( balance.add(value),  codeHash);
    }
}
