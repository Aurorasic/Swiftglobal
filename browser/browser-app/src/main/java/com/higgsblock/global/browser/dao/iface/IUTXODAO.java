package com.higgsblock.global.browser.dao.iface;

import com.higgsblock.global.browser.dao.entity.UTXOPO;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-25
 */
public interface IUTXODAO extends IDAO<UTXOPO> {

    /**
     * batch insert
     *
     * @param utxoPos
     * @return
     */
    int[] batchInsert(List<UTXOPO> utxoPos);

    /**
     * @param transactionHash
     * @param index
     * @return
     */

    /**
     * delete UTXOPO by transactionHash and index
     * @param transactionHash
     * @param index
     * @return
     */
    int deleteUTXO(String transactionHash, short index);
}
