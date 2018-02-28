package cn.primeledger.cas.global.utils;

import java.math.BigDecimal;

/**
 * @author yuguojia
 * @create 2018-02-26
 **/
public class AmountUtils {
    public static BigDecimal MAX_AMOUNT = new BigDecimal("999999999999999999.9999999999");//18+10

    public static boolean check(boolean canEqualZero, BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            return false;
        }
        if (!canEqualZero && amount.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        return true;
    }
}