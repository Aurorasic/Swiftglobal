package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;

import java.util.List;

/**
 * Utxo service that only handle utxo on best chain blocks
 *
 * @author yuguojia
 * @date 2018/06/29
 **/
public interface IBestUTXOService {
    void saveUTXO(UTXO utxo);

    UTXO getUTXOByKey(String utxoKey);

    /**
     * find by lockScript(address) and currency
     *
     * @param lockScript
     * @param currency
     * @return
     */
    List<UTXOEntity> findByLockScriptAndCurrency(String lockScript, String currency);

    List<UTXO> getUTXOsByAddress(String addr);

    /**
     * delete by txHash and outIndex
     * @param transactionHash
     * @param outIndex
     */
    void deleteByTransactionHashAndOutIndex(String transactionHash,short outIndex);

}
