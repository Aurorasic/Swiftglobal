package com.higgsblock.global.browser.service.iface;

import com.higgsblock.global.browser.dao.entity.UTXOPO;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-25
 */
public interface IUTXOService {

    /**
     * batch insert
     *
     * @param utxoPos
     */
    void batchInsert(List<UTXOPO> utxoPos);

    /**
     * delete UTXOPO by transactionHash and index
     *
     * @param transactionHash
     * @param index
     * @return
     */
    boolean deleteUTXO(String transactionHash, short index);

    /**
     * batch delete UTXOPO by transactionHash_index
     *
     * @param txHashIndexs
     * @return
     */
    boolean batchDeleteUTXO(List<String> txHashIndexs);

    /**
     * get utxoList by address
     *
     * @param address
     * @return
     */
    List<UTXOPO> getUTXOsByAddress(String address);
}
