package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;

import java.util.Set;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-21 12:38
 **/
public interface ITransactionService {

    public boolean validTransactions(Block block);


    public void receivedTransaction(Transaction tx);

    public boolean hasStake(String address, SystemCurrencyEnum currency);

    public boolean hasStake(String preBlockHash, String address, SystemCurrencyEnum currency);

    public Set<String> getRemovedMiners(Transaction tx);

    public Set<String> getAddedMiners(Transaction tx);
}