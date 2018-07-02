package com.higgsblock.global.chain.app.dao.iface;


import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-09
 */
public interface IUTXOEntity extends IDao<UTXOEntity> {

    /**
     * Query according to the specified field.
     *
     * @param transactionHash
     * @param outIndex
     * @return
     */
    UTXOEntity getByField(String transactionHash, short outIndex);

    /**
     * Delete according to the specified field.
     *
     * @param transactionHash
     * @param outIndex
     * @return
     */
    int delete(String transactionHash, short outIndex);


    List<UTXOEntity> selectByAddressCurrency(String address, String currency);


    List<UTXOEntity> selectByAddress(String address);

}
