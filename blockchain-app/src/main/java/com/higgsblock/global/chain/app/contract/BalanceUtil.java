package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.common.utils.Money;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * money gas convert
 * @author tangkun
 * @date 2018-09-14
 */
public class BalanceUtil {

    /**
     * 1000000000 gas = 1 money
     */
    public static final Money GAS_TO_MONEY = new Money("100000000");

    /**
     * gas convert to money
     * @param gas
     * @return money
     */
    public static Money convertGasToMoney(BigInteger gas){

        return  new Money(gas.intValue()).divide(GAS_TO_MONEY);

    }

    /**
     * money convert to gas
     * @param money
     * @return money
     */
    public static BigInteger convertMoneyToGas(Money money){

        Money temp = money.multiply(GAS_TO_MONEY);

        return  new BigInteger(new BigDecimal(temp.getValue()).stripTrailingZeros().toPlainString());
    }
}
