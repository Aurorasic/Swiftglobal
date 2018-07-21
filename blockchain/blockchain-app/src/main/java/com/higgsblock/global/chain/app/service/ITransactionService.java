package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-21 12:38
 **/
public interface ITransactionService {

    public boolean validTransactions(Block block);
}