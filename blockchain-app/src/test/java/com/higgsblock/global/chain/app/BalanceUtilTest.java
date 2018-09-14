package com.higgsblock.global.chain.app;

import com.higgsblock.global.chain.app.contract.BalanceUtil;
import com.higgsblock.global.chain.common.utils.Money;
import org.junit.Test;

import java.math.BigInteger;

/**
 * @author tangkun
 * @date 2018-09-14
 */
public class BalanceUtilTest {

    public static final Money money = new Money("10.09090909");
    public static final BigInteger gas = new BigInteger("1");

    @Test
    public void testConvertGasToMoney() throws Exception {
        System.out.println(BalanceUtil.convertGasToMoney(gas).getValue());

    }

    @Test
    public void testConvertMoneyToGas() throws Exception {
        System.out.println(BalanceUtil.convertMoneyToGas(money));
    }
}