package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.SpentTransactionOutIndexEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/13
 */
public interface ISpentTransactionOutIndexRepository extends JpaRepository<SpentTransactionOutIndexEntity, Long> {

    /**
     * find SpentTransactionOutIndexEntity list by preTransactionHash
     *
     * @param preTransactionHash
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    List<SpentTransactionOutIndexEntity> findByPreTransactionHash(String preTransactionHash);

}
