package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;

import java.util.List;

/**
 * The interface Transaction service.
 *
 * @author Zhao xiaogang
 * @date 2018 -05-22
 */
public interface ITransactionIndexService {

    /**
     * find by txHash
     *
     * @param txHash
     * @return
     */
    TransactionIndexEntity findByTransactionHash(String txHash);

    /**
     * Add transaction index and utxo in database
     *
     * @param bestBlock     the best block
     * @param bestBlockHash the best block hash
     * @throws Exception the exception
     */
    void addTransIdxAndUtxo(Block bestBlock, String bestBlockHash) throws Exception;

    /**
     * getTxOfUnSpentUtxo
     *
     * @param cacheTransactions cached transactions
     * @return void
     */
    List<Transaction> getTxOfUnSpentUtxo(String preBlockHash, List<Transaction> cacheTransactions);

}
