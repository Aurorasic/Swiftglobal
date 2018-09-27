package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
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
    Map<String, Money> get(String address);

    /**
     * Plus balance.
     *
     * @param utxo the utxo
     */
    void plusBalance(UTXO utxo);

    /**
     * Minus balance.
     *
     * @param utxo the utxo
     */
    void minusBalance(UTXO utxo);


    /**
     * get balance on confirm block chain and unconfirmed block chain(from the preBlockHash to best block)
     * from the max height first block
     *
     * @param preBlockHash the pre block hash
     * @param address      the address
     * @param currency     the currency
     * @return the union balance
     */
    Money getUnionBalance(String preBlockHash, String address, String currency);
}