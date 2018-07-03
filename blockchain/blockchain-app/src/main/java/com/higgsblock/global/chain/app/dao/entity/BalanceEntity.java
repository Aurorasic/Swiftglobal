package com.higgsblock.global.chain.app.dao.entity;

import com.higgsblock.global.chain.common.utils.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Balance entity.
 *
 * @author yanghuadong
 * @date 2018 -07-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceEntity {

    /**
     * The Address.
     */
    private String address;

    /**
     * The Amount.
     */
    private String amount;

    /**
     * The Currency.
     */
    private String currency;

    /**
     * Gets money.
     *
     * @return the money
     */
    public Money getMoney() {
        return new Money(amount, currency);
    }
}