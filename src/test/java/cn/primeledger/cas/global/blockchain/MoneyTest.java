package cn.primeledger.cas.global.blockchain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author Su Jiulong
 * @date 2018/2/27
 */
public class MoneyTest {
    //Test object
    Money money;

    @Before
    public void setUp() {
        //Initialize the test object.
        money = new Money("199938443");
    }

    @Test
    public void getAmount() throws Exception {
        Assert.assertEquals("199938443.00000000", money.getAmount().toString());
    }

    @Test
    public void getAmountValue() throws Exception {
        Assert.assertEquals("199938443.00000000", money.getAmountValue());
    }

    @Test
    public void setAmount() throws Exception {
        money.setAmount(new BigDecimal("123"));
        Assert.assertEquals("123.00000000", money.getAmount().toString());
    }

    @Test
    public void getCurrency() throws Exception {
        Assert.assertEquals("CAS", money.getCurrency());
    }

    @Test
    public void equalsTrue() throws Exception {
        Assert.assertTrue(money.equals(money));
    }

    @Test
    public void equalsObjectIsNullFalse() throws Exception {
        Assert.assertFalse(money.equals(null));
    }

    @Test
    public void equalsObjectGetClassFalse() throws Exception {
        Assert.assertFalse(money.equals(new Object()));
    }

    @Test
    public void equals() throws Exception {
        //The amount is the same but the currency is different.
        Object moneyObject = new Money("199938443", "BTC");
        Assert.assertFalse(money.equals(moneyObject));
        //The currency is the same but the amount varies.
        moneyObject = new Money("234");
        Assert.assertFalse(money.equals(moneyObject));
        //The currency is different and the amount is different.
        moneyObject = new Money("234", "BTC");
        Assert.assertFalse(money.equals(moneyObject));
        //The amount of the same currency is the same.
        moneyObject = new Money("199938443");
        Assert.assertTrue(money.equals(moneyObject));
    }

    @Test
    public void hashCodeTest() throws Exception {
        System.out.println("The money's hashCode is " + money.hashCode());
    }

    @Test
    public void compareToObject() throws Exception {
        //The specified amount equal to the parameter,which return 0.
        Object money1 = new Money("199938443");
        Assert.assertEquals(0, money.compareTo(money1));
        //The specified amount is less than the parameter and returns -1.
        money1 = new Money("1999384431");
        Assert.assertEquals(-1, money.compareTo(money1));
        //The specified amount is greater than the parameter and returns 1.
        money1 = new Money("1999");
        Assert.assertEquals(1, money.compareTo(money1));
    }

    @Test
    public void compareToMoney() throws Exception {
        //The currency is different and the amount is different.
        Money money1 = new Money("12334", "BTC");
        try {
            money.compareTo(money1);
        } catch (Exception e) {
            Assert.assertEquals("Currency mismatch.", e.getMessage());
        }

        //Different currency, same amount.
        money1 = new Money("199938443", "BTC");
        try {
            money.compareTo(money1);
        } catch (Exception e) {
            Assert.assertEquals("Currency mismatch.", e.getMessage());
        }

        //The specified amount equal to the parameter,which return 0.
        Assert.assertEquals(0, money.compareTo(new Money("199938443")));
        //The specified amount is less than the parameter and returns -1.
        Assert.assertEquals(-1, money.compareTo(new Money("1999384431")));
        //The specified amount is greater than the parameter and returns 1.
        Assert.assertEquals(1, money.compareTo(new Money("1999")));
    }

    @Test
    public void greaterThan() throws Exception {
        //The specified amount equal to the parameter,which return false.
        Assert.assertFalse(money.greaterThan(new Money("199938443")));
        //The specified amount is less than the parameter and returns false.
        Assert.assertFalse(money.greaterThan(new Money("1999384431")));
        //The specified amount is greater than the parameter and returns true.
        Assert.assertTrue(money.greaterThan(new Money("1999")));
    }

    @Test
    public void addBigDecimal() throws Exception {
        money = money.add(new BigDecimal("111"));
        Assert.assertEquals("199938554.00000000", money.getAmount().toString());
    }

    @Test
    public void addLong() throws Exception {
        money = money.add(222L);
        Assert.assertEquals("199938665.00000000", money.getAmount().toString());
    }

    @Test
    public void addString() throws Exception {
        money = money.add("111");
        Assert.assertEquals("199938554.00000000", money.getAmount().toString());
    }

    @Test
    public void subtractBigDecimal() throws Exception {
        money = money.subtract(new BigDecimal("443"));
        Assert.assertEquals("199938000.00000000", money.getAmount().toString());
    }

    @Test
    public void subtractLong() throws Exception {
        money = money.subtract(443L);
        Assert.assertEquals("199938000.00000000", money.getAmount().toString());
    }

    @Test
    public void subtractString() throws Exception {
        money = money.subtract("443");
        Assert.assertEquals("199938000.00000000", money.getAmount().toString());
    }

    @Test
    public void multiplyBigDecimal() throws Exception {
        money = money.multiply(new BigDecimal("10"));
        Assert.assertEquals("1999384430.00000000", money.getAmount().toString());
    }

    @Test
    public void multiplyLong() throws Exception {
        money = money.multiply(10L);
        Assert.assertEquals("1999384430.00000000", money.getAmount().toString());
    }

    @Test
    public void multiplyString() throws Exception {
        money = money.multiply("10");
        Assert.assertEquals("1999384430.00000000", money.getAmount().toString());
    }

    @Test
    public void divideBigDecimal() throws Exception {
        //The divisor is zero.
        try {
            money = money.divide(new BigDecimal("0"));
        } catch (Exception e) {
            if (e instanceof ArithmeticException) {
                Assert.assertTrue(e.getMessage().contains("/ by zero"));
            }
        }

        money = money.divide(new BigDecimal("10"));
        Assert.assertEquals("19993844.30000000", money.getAmount().toString());
    }

    @Test
    public void divide1Long() throws Exception {
        money = money.divide(10L);
        Assert.assertEquals("19993844.30000000", money.getAmount().toString());
    }

    @Test
    public void divideString() throws Exception {
        money = money.divide("10");
        Assert.assertEquals("19993844.30000000", money.getAmount().toString());
    }

}