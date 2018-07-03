package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.common.utils.Money;

import java.util.Map;

/**
 * The interface Balance entity.
 *
 * @author yanghuadong
 * @date 2018 -07-02
 */
public interface IBalanceEntity extends IDao<BalanceEntity> {
    /**
     * Gets all balances.
     *
     * @return the all balances
     */
    Map<String, Money> getAllBalances();

    /**
     * Gets balance.
     *
     * @param address  the address
     * @param currency the currency
     * @return the balance
     */
    BalanceEntity getBalance(String address, String currency);
}