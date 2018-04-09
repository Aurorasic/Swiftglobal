package cn.primeledger.cas.global.blockchain;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author baizhengwen
 * @date 2018-02-27
 */
public class Money implements Serializable, Comparable {

    private static final long serialVersionUID = 6009335074727417445L;

    private BigDecimal amount;

    /**
     * 币种。
     */
    private String currency = "CAS";

    /**
     * 小数位数
     */
    private int decimalDigits = 8;

    public Money(String val) {
        this.amount = newAmount(val);
    }

    public Money(String val, String currency) {
        this.amount = newAmount(val);
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return newAmount(getAmountValue());
    }

    public void setAmount(BigDecimal val) {
        if (val != null) {
            this.amount = newAmount(val);
        }
    }

    public String getAmountValue() {
        return amount.toString();
    }

    public String getCurrency() {
        return currency;
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
        return Objects.equal(amount, money.amount) &&
                Objects.equal(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount, currency);
    }

    @Override
    public int compareTo(Object other) {
        return compareTo((Money) other);
    }

    public int compareTo(Money other) {
        assertSameCurrencyAs(other);
        return amount.compareTo(other.getAmount());
    }

    public boolean greaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public Money add(BigDecimal val) {
        amount = amount.add(val);
        return this;
    }

    public Money add(long val) {
        return add(newAmount(val));
    }

    public Money add(String val) {
        return add(newAmount(val));
    }

    public Money subtract(BigDecimal val) {
        amount = amount.subtract(val);
        return this;
    }

    public Money subtract(long val) {
        return subtract(newAmount(val));
    }

    public Money subtract(String val) {
        return subtract(newAmount(val));
    }

    public Money multiply(BigDecimal val) {
        amount = amount.multiply(val).setScale(decimalDigits, RoundingMode.HALF_EVEN);
        return this;
    }

    public Money multiply(long val) {
        return multiply(newAmount(val));
    }

    public Money multiply(String val) {
        return multiply(newAmount(val));
    }

    public Money divide(BigDecimal val) {
        amount = amount.divide(val, decimalDigits, RoundingMode.HALF_EVEN);
        return this;
    }

    public Money divide(long val) {
        return divide(newAmount(val));
    }

    public Money divide(String val) {
        return divide(newAmount(val));
    }

    private void assertSameCurrencyAs(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch.");
        }
    }

    private BigDecimal newAmount(String val) {
        return new BigDecimal(val).setScale(decimalDigits, RoundingMode.HALF_EVEN);
    }

    private BigDecimal newAmount(long val) {
        return new BigDecimal(val).setScale(decimalDigits, RoundingMode.HALF_EVEN);
    }

    private BigDecimal newAmount(BigDecimal val) {
        return newAmount(val.toString());
    }
}
