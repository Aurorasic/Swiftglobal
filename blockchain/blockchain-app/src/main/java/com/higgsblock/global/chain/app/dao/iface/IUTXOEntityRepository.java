package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public interface IUTXOEntityRepository extends JpaRepository<UTXOEntity, Long> {

    /**
     * find by txHash and outIndex
     *
     * @param transactionHash
     * @param outIndex
     * @return
     */
    UTXOEntity findByTransactionHashAndOutIndex(String transactionHash, short outIndex);

    /**
     * delete by txHash and outIndex
     *
     * @param transactionHash
     * @param outIndex
     */
    @Query(value = "delete from UTXOEntity where transactionHash=:transactionHash and outIndex=:outIndex", nativeQuery = false)
    @Modifying
    void deleteByTransactionHashAndOutIndex(@Param("transactionHash") String transactionHash, @Param("outIndex") short outIndex);

    /**
     * find by lockScript(address)
     *
     * @param lockScript
     * @return
     */
    List<UTXOEntity> findByLockScript(String lockScript);

    /**
     * find by lockScript(address) and currency
     *
     * @param lockScript
     * @param currency
     * @return
     */
    List<UTXOEntity> findByLockScriptAndCurrency(String lockScript, String currency);

}
