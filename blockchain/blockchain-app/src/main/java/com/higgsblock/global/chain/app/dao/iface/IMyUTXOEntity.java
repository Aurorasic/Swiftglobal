package com.higgsblock.global.chain.app.dao.iface;


/**
 * @author Su Jiulong
 * @date 2018-05-10
 */
public interface IMyUTXOEntity extends IDao<MyUTXOEntity> {
    /**
     * Query according to the specified field.
     *
     * @param transactionHash
     * @param outIndex
     * @return
     */
    MyUTXOEntity getByField(String transactionHash, short outIndex);

    /**
     * Delete according to the specified field.
     *
     * @param transactionHash
     * @param outIndex
     * @return
     */
    int delete(String transactionHash, short outIndex);


}
