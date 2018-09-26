package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.common.utils.Money;

import java.util.Map;

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

    /**
     * Get map.
     *
     * @param address the address
     * @return the map
     */
    Map<String,Money> get(String address);

    /**
     * Save.
     *
     * @param block the block
     */
    void save(Block block);

    /**
     * get balance on confirm block chain and unconfirmed block chain(from the preBlockHash to best block)
     * from the max height first block
     */
    Money getUnionBalance(String preBlockHash, String address, String currency);
}