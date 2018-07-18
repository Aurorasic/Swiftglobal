package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;

import java.util.List;

/**
 * The interface Transaction service.
 *
 * @author Zhao xiaogang
 * @date 2018 -05-22
 */
public interface ITransService {

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

    /**
     * Gets utxo.
     *
     * @param utxoKey the utxo key
     * @return the utxo
     */
    UTXO getUTXOOnBestChain(String utxoKey);

    /**
     * Query the all UTXOS according the address
     *
     * @param addr
     * @return
     */
    List<UTXO> getUTXOsByAddress(String addr);

    /**
     * delete by txHash and outIndex
     * @param transactionHash
     * @param outIndex
     */
    void deleteByTransactionHashAndOutIndex(String transactionHash,short outIndex);
}
