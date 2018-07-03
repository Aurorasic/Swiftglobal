package com.higgsblock.global.chain.app.dao.iface;


import com.higgsblock.global.chain.app.dao.entity.SpentTransactionOutIndexEntity;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-12
 */
public interface ISpentTransactionOutIndexEntity {

    /**
     * add data
     *
     * @param spentTransactionOutIndexEntity
     * @return
     */
    int add(SpentTransactionOutIndexEntity spentTransactionOutIndexEntity);


    /**
     * Get the data based on the hash in the previous block.
     *
     * @param preTxHash
     * @return
     */
    List<SpentTransactionOutIndexEntity> getByPreHash(String preTxHash);
}
