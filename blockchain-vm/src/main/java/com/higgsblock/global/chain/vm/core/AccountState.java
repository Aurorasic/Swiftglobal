package com.higgsblock.global.chain.vm.core;

import com.higgsblock.global.chain.vm.util.ByteArrayWrapper;
import com.higgsblock.global.chain.vm.util.FastByteComparisons;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.higgsblock.global.chain.vm.util.HashUtil.EMPTY_DATA_HASH;
import static com.higgsblock.global.chain.vm.util.HashUtil.sha3;

/**
 * @author tangkun
 * @date 2018-09-06
 */
public class AccountState implements Serializable {

    /* A value equal to the number of transactions sent
     * from this address, or, in the case of contract accounts,
     * the number of contract-creations made by this account */
    private long nonce = 0;

    private BigInteger balance = BigInteger.ZERO;

    private byte[] codeHash;

    private String currency;

    private Set<ByteArrayWrapper> keys;

    public AccountState(long nonce, BigInteger balance, byte[] codeHash, String currency, Set<ByteArrayWrapper> keys) {
        this.nonce = nonce;
        this.balance = balance;
        this.codeHash = codeHash;
        this.currency = currency;
        this.keys = keys;
    }

//    public AccountState(long nonce, BigInteger balance, byte[] codeHash, String currency) {
//        this.nonce = nonce;
//        this.balance = balance;
//        this.codeHash = codeHash;
//        this.currency = currency;
//    }

    public AccountState(long nonce, BigInteger balance) {
        this(nonce, balance, EMPTY_DATA_HASH, "", new HashSet<>());
    }

    public AccountState withIncrementedNonce() {
        return new AccountState(nonce += 1, balance, codeHash, currency, keys);
    }

    public AccountState withCodeHash(byte[] codeHash) {
        return new AccountState(nonce, balance, codeHash, currency, keys);
    }

    public long getNonce() {
        return nonce;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public byte[] getCodeHash() {
        return codeHash;
    }

    public Set<ByteArrayWrapper> getKeys() {
        return keys;
    }

    public AccountState withBalanceIncrement(BigInteger value) {
        this.balance = balance.add(value);
        return new AccountState(nonce, balance.add(value), codeHash, currency, keys);
    }

    public AccountState withBalanceDecrement(BigInteger value) {
        this.balance = balance.subtract(value);
        return new AccountState(nonce, balance.add(value), codeHash, currency, keys);
    }

    public AccountState withStorageKey(ByteArrayWrapper value) {
        this.keys.add(value);
        return new AccountState(nonce, balance, codeHash, currency, keys);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isEmpty() {
        return bytesEqual(codeHash, sha3(new byte[0])) &&
                BigInteger.ZERO.equals(balance);
    }

    private boolean bytesEqual(byte[] b1, byte[] b2) {
        return b1.length == b2.length && compareTo(b1, 0, b1.length, b2, 0, b2.length) == 0;
    }

    private int compareTo(byte[] buffer1, int offset1, int length1,
                          byte[] buffer2, int offset2, int length2) {
        // Short circuit equal case
        if (buffer1 == buffer2 &&
                offset1 == offset2 &&
                length1 == length2) {
            return 0;
        }
        int end1 = offset1 + length1;
        int end2 = offset2 + length2;
        for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {
            int a = (buffer1[i] & 0xff);
            int b = (buffer2[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return length1 - length2;
    }

    public boolean isContractExist() {
        return !FastByteComparisons.equal(codeHash, EMPTY_DATA_HASH) ||
                nonce != 0;
    }

    @Override
    public String toString() {
        return "AccountState{" +
                "nonce=" + nonce +
                ", balance=" + balance +
                ", codeHash=" + Arrays.toString(codeHash) +
                ", currency='" + currency + '\'' +
                ", keys=" + keys.stream().map(item -> item.toString()).collect(Collectors.joining(" ")) +
                '}';
    }
}
