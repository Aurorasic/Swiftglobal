package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Rewards;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.common.utils.Money;

import java.util.List;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-21 18:14
 **/
public interface ITransactionFeeService {

    Rewards countMinerAndWitnessRewards(Money fee, long height);

    Transaction buildCoinBaseTx(long lockTime, short version, Money fee, long height);
    
    /**
     * sort by transaction gasPrice
     *
     * @param transactions wait sort
     * @return sorted transaction by gasPrice
     */
    void sortByGasPrice(List<Transaction> transactions);
}