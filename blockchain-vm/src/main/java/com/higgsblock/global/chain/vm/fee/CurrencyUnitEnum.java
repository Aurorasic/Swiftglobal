package com.higgsblock.global.chain.vm.fee;

import java.math.BigInteger;

/**
 * Currency unit introduced with contract execution.
 *
 * @author Chen Jiawei
 * @date 2018-09-26
 */
public enum CurrencyUnitEnum {
    /**
     * basic atomic unit.
     */
    HIG(BigInteger.valueOf(1L)),
    /**
     * 1KHIG = 10^3HIG.
     */
    KHIG(BigInteger.valueOf(1_000L)),
    /**
     * 1MHIG = 10^6HIG.
     */
    MHIG(BigInteger.valueOf(1_000_000L)),
    /**
     * 1GHIG = 10^9HIG.
     */
    GHIG(BigInteger.valueOf(1_000_000_000L)),
    /**
     * 1CAS = 10^18HIG.
     */
    CAS(BigInteger.valueOf(1_000_000_000_000_000_000L));

    BigInteger weight;

    CurrencyUnitEnum(BigInteger weight) {
        this.weight = weight;
    }

    public BigInteger getWeight() {
        return weight;
    }
}
