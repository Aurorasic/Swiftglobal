package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-21 12:38
 **/
public interface ITransactionService {

    public boolean validTransactions(Block block);


    public void receivedTransaction(Transaction tx);
}