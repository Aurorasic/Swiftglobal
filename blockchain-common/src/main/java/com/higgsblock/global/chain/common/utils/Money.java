package com.higgsblock.global.chain.common.utils;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author baizhengwen
 * @date 2018-02-27
 */
@JSONType(includes = {"value", "currency"})
public class Money extends BaseSerializer implements Comparable {

    private static final long serialVersionUID = 6009335074727417445L;
    private static final String MAX_AMOUNT = "999999999999999999";
    private static final String MIN_AMOUNT = "0";

    private BigDecimal value;

    /**
     * currency
     */
    @Getter
    @Setter
    private String currency;

    /**
     * Precise digits
     */
    private int decimalDigits = 18;

    public Money() {
        this(0);
    }

    public Money(String val) {
        this(val, SystemCurrencyEnum.CAS.getCurrency());
    }

    public Money(long val) {
        this(val, SystemCurrencyEnum.CAS.getCurrency());
    }

    public Money(String val, String currency) {
        this.value = newBigDecimal(val);
        this.currency = currency;
    }

    public Money(long val, String currency) {
        this.value = newBigDecimal(val);
        this.currency = currency;
    }

    public String getHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder()
                .append(function.hashString(null == currency ? StringUtils.EMPTY : currency, Charsets.UTF_8))
                .append(function.hashString(null == value ? StringUtils.EMPTY : value.toString(), Charsets.UTF_8));
        return function.hashString(builder, Charsets.UTF_8).toString();
    }

    public String getValue() {
        return value.toString();
    }

    public void setValue(String val) {
        this.value = newBigDecimal(val);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Money money = (Money) o;
        return Objects.equal(value, money.value) &&
                Objects.equal(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, currency);
    }

    @Override
    public int compareTo(Object other) {
        return compareTo((Money) other);
    }

    public int compareTo(Money other) {
        checkCurrency(other);
        return value.compareTo(newBigDecimal(other.getValue()));
    }

    public int compareTo(String val) {
        return value.compareTo(newBigDecimal(val));
    }

    public int compareTo(long val) {
        return value.compareTo(newBigDecimal(val));
    }

    public boolean equals(long val) {
        return compareTo(val) == 0;
    }

    public boolean equals(String val) {
        return compareTo(val) == 0;
    }

    public boolean equals(Money other) {
        return compareTo(other) == 0;
    }

    public boolean greaterThan(long val) {
        return compareTo(val) > 0;
    }

    public boolean greaterThan(String val) {
        return compareTo(val) > 0;
    }

    public boolean greaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public boolean lessThan(long val) {
        return compareTo(val) < 0;
    }

    public boolean lessThan(String val) {
        return compareTo(val) < 0;
    }

    public boolean lessThan(Money other) {
        return compareTo(other) < 0;
    }

    public Money add(long val) {
        return add(newBigDecimal(val));
    }

    public Money add(String val) {
        return add(newBigDecimal(val));
    }

    public Money add(Money money) {
        checkCurrency(money);
        return add(money.getValue());
    }

    public Money subtract(long val) {
        return subtract(newBigDecimal(val));
    }

    public Money subtract(String val) {
        return subtract(newBigDecimal(val));
    }

    public Money subtract(Money money) {
        checkCurrency(money);
        return subtract(money.getValue());
    }

    public Money multiply(long val) {
        return multiply(newBigDecimal(val));
    }

    public Money multiply(String val) {
        return multiply(newBigDecimal(val));
    }

    public Money multiply(Money money) {
        checkCurrency(money);
        return multiply(money.getValue());
    }

    public Money divide(long val) {
        return divide(newBigDecimal(val));
    }

    public Money divide(String val) {
        return divide(newBigDecimal(val));
    }

    public Money divide(Money money) {
        checkCurrency(money);
        return divide(money.getValue());
    }

    public boolean checkRange(String maxValue, String minValue) {
        return !greaterThan(maxValue) && !lessThan(minValue);
    }

    public boolean checkRange() {
        return checkRange(MAX_AMOUNT, MIN_AMOUNT);
    }

    private Money add(BigDecimal val) {
        value = value.add(val);
        return this;
    }

    private Money subtract(BigDecimal val) {
        value = value.subtract(val);
        return this;
    }

    private Money multiply(BigDecimal val) {
        value = value.multiply(val).setScale(decimalDigits, RoundingMode.HALF_UP);
        return this;
    }

    private Money divide(BigDecimal val) {
        value = value.divide(val, decimalDigits, RoundingMode.HALF_UP);
        return this;
    }

    private void checkCurrency(Money other) {
        if (!StringUtils.equals(currency, other.currency)) {
            throw new IllegalArgumentException("Currency mismatch.");
        }
    }

    private BigDecimal newBigDecimal(String val) {
        return new BigDecimal(val).setScale(decimalDigits, RoundingMode.HALF_UP);
    }

    private BigDecimal newBigDecimal(long val) {
        return new BigDecimal(val).setScale(decimalDigits, RoundingMode.HALF_UP);
    }

}
