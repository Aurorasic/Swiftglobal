package com.higgsblock.global.chain.app.dao.iface;


import com.higgsblock.global.chain.app.blockchain.transaction.TransactionIndex;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;

/**
 * @author yangshenghong
 * @date 2018-05-08
 */
public interface ITransactionIndexEntity extends IDao<TransactionIndexEntity> {

    TransactionIndex get(String transactionHash);

}
