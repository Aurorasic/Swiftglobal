package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.common.utils.Money;

/**
 * The interface Balance service.
 *
 * @author yanghuadong
 * @date 2018 -09-26
 */
public interface IBalanceService {
    /**
     * Gets balance.
     *
     * @param address  the address
     * @param currency the currency
     * @return the money
     */
    Money getBalanceOnBest(String address, String currency);
}