package com.higgsblock.global.chain.vm.core;

import java.math.BigInteger;

/**
 * @author tangkun
 * @date 2018-09-06
 */
public class AccountState {

    private  BigInteger balance;

    private  byte[] codeHash;

    private String currency;

    public AccountState(BigInteger balance,byte[] codeHash,String currency){
        this.balance = balance;
        this.codeHash = codeHash;
        this.currency = currency;
    }

    public AccountState(BigInteger balance,byte[] codeHash){
        this.balance=balance;
        this.codeHash=codeHash;
    }



    public AccountState withCodeHash(byte[] codeHash) {
        return new AccountState(balance,  codeHash,currency);
    }

    public BigInteger getBalance() {
        return balance;
    }

    public byte[] getCodeHash() {
        return codeHash;
    }

    public AccountState withBalanceIncrement(BigInteger value) {
        return new AccountState( balance.add(value),  codeHash,currency);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
