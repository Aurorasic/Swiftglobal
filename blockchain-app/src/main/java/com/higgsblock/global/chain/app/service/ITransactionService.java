package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;

import java.util.Set;

/**
 * The interface Transaction service.
 *
 * @description:
 * @author: yezaiyong
 * @create: 2018 -07-21 12:38
 */
public interface ITransactionService {

    /**
     * Valid transactions boolean.
     *
     * @param block the block
     * @return the boolean
     */
    boolean validTransactions(Block block);

    /**
     * Received transaction.
     *
     * @param tx the tx
     */
    void receivedTransaction(Transaction tx);

    /**
     * Has stake boolean.
     *
     * @param address  the address
     * @param currency the currency
     * @return the boolean
     */
    boolean hasStakeOnBest(String address, SystemCurrencyEnum currency);

    /**
     * Has stake boolean.
     *
     * @param preBlockHash the pre block hash
     * @param address      the address
     * @param currency     the currency
     * @return the boolean
     */
    boolean hasStake(String preBlockHash, String address, SystemCurrencyEnum currency);

    /**
     * Gets removed miners.
     *
     * @param tx the tx
     * @return the removed miners
     */
    Set<String> getRemovedMiners(Transaction tx);

    /**
     * Gets added miners.
     *
     * @param tx the tx
     * @return the added miners
     */
    Set<String> getAddedMiners(Transaction tx);

    /**
     * calculation ordinary transaction fee
     *
     * @param tx Ordinary Transaction
     * @return cas fee
     */
    Money calculationOrdinaryTransactionFee(Transaction tx);
}