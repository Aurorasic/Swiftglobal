package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Rewards;
import com.higgsblock.global.chain.app.blockchain.transaction.SortResult;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.common.utils.Money;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-21 18:14
 **/
public interface ITransactionFeeService {

    public SortResult orderTransaction(String preBlockHash, List<Transaction> txList);

    public List<Transaction> getCanPackageTransactionsOfBlock(List<Transaction> txList);

    public Money getCurrencyFee(Transaction tx);

    Rewards countMinerAndWitnessRewards(Money fee, long height);

    Transaction buildCoinBaseTx(long lockTime, short version, Money fee, long height);

    /**
     * choose from sortedTransaction  if transaction is contract transaction
     * @param sortedTransaction sorted transactions
     * @return block include transaction
     */
    List<Transaction> chooseAndInvokedTransaction(List<Transaction> sortedTransaction);

    /**
     * sort by transaction gasPrice
     * @param transactions wait sort
     * @return sorted transaction by gasPrice
     */
    void sortByGasPrice(List<Transaction> transactions);
}