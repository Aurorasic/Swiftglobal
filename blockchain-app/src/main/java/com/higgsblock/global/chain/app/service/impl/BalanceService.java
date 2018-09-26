package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.service.IBalanceService;
import com.higgsblock.global.chain.common.utils.Money;
import org.springframework.stereotype.Service;

/**
 * The type Balance service.
 *
 * @author yanghuadong
 * @date 2018 -09-26
 */
@Service
public class BalanceService implements IBalanceService {

    /**
     * Gets balance.
     *
     * @param address  the address
     * @param currency the currency
     * @return the money
     */
    @Override
    public Money getBalanceOnBest(String address, String currency) {
        return null;
    }
}