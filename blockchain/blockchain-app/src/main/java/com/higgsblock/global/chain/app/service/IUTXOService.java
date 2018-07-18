package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;

import java.util.List;

/**
 * @author yuguojia
 * @date 2018/06/29
 **/
public interface IUTXOService {
    void saveUTXO(UTXO utxo);

    UTXO getUTXOOnBestChain(String utxoKey);


    List<UTXO> getUnionUTXO(String preBlockHash, String address, String currency);


    UTXO getUnionUTXO(String preBlockHash, String utxoKey);


    boolean isRemovedUTXORecurse(String blockHash, String utxoKey);


    void addNewBlock(Block newBestBlock, Block newBlock);


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
