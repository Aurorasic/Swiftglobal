package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public interface IUTXORepository extends IKeyValueRepository<UTXOEntity, String> {

    /**
     * find by txHash and outIndex
     *
     * @param transactionHash
     * @param outIndex
     * @return
     */
    @IndexQuery("transactionHash")
    UTXOEntity findByTransactionHashAndOutIndex(String transactionHash, short outIndex);

    /**
     * delete by txHash and outIndex
     *
     * @param transactionHash
     * @param outIndex
     */
    @IndexQuery("transactionHash")
    void deleteByTransactionHashAndOutIndex(@Param("transactionHash") String transactionHash, @Param("outIndex") short outIndex);

    /**
     * find by lockScript(address)
     *
     * @param lockScript
     * @return
     */
    @IndexQuery("lockScript")
    List<UTXOEntity> findByLockScript(String lockScript);

    /**
     * find by lockScript(address) and currency
     *
     * @param lockScript
     * @param currency
     * @return
     */
    @IndexQuery("lockScript")
    List<UTXOEntity> findByLockScriptAndCurrency(String lockScript, String currency);

}
