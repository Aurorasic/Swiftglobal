package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;

import java.util.List;

/**
 * @author yuguojia
 * @date 2018/06/29
 **/
public interface IUTXOService {

    UTXO getUTXOOnBestChain(String utxoKey);


    List<UTXO> getUnionUTXO(String preBlockHash, String address, String currency);


    UTXO getUnionUTXO(String preBlockHash, String utxoKey);


    boolean isRemovedUTXORecurse(String blockHash, String utxoKey);


    void addNewBlock(Block newBestBlock, Block newBlock);

}
