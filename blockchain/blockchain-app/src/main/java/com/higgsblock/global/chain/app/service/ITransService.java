package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;

import java.util.List;

/**
 * @author Zhao xiaogang
 * @date 2018-05-22
 */
public interface ITransService {

    /**
     * Add transaction index and utxo in database
     *
     * @param bestBlock the best block
     * @param bestBlockHash the best block hash
     * @throws  Exception
     * @return List<BaseDaoEntity>
     */
    List<BaseDaoEntity> addTransIdxAndUtxo(Block bestBlock, String bestBlockHash) throws Exception;

    /**
     * Remove double spent transaction
     *
     * @param cacheTransactions cached transactions
     * @return void
     */
    void removeDoubleSpendTx(List<Transaction> cacheTransactions);
}
