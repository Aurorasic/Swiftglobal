package com.higgsblock.global.chain.vm.fee;

import java.math.BigInteger;

/**
 * @author Chen Jiawei
 * @date 2018-09-26
 */
public class FeeUtil {
    private static final long SIZE_GAS = 65;

    public static BigInteger getHigAmount(long amount, CurrencyUnitEnum currencyUnit) {
        return BigInteger.valueOf(amount).multiply(currencyUnit.getWeight());
    }

    public static BigInteger getSizeGas(long size) {
        return BigInteger.valueOf(SIZE_GAS).multiply(BigInteger.valueOf(size));
    }
}
