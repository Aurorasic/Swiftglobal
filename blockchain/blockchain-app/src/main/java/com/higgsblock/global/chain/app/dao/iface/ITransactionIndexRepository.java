package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author yangshenghong
 * @date 2018-07-13
 */
public interface ITransactionIndexRepository extends JpaRepository<TransactionIndexEntity, Long> {
    /**
     * find by txHash
     *
     * @param txHash
     * @return
     */
    TransactionIndexEntity findByTransactionHash(String txHash);
}
