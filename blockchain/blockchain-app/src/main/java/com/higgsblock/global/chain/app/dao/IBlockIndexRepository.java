package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IBlockIndexRepository extends JpaRepository<BlockIndexEntity, Long> {

    /**
     * find BlockIndexEntity by blockHash
     *
     * @param blockHash
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    BlockIndexEntity findByBlockHash(String blockHash);

    /**
     * find all BlockIndexEntities by height
     *
     * @param height
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    List<BlockIndexEntity> findAllByHeight(long height);

    /**
     * query BlockIndexEntity records max height
     *
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    @Query("select max (height) from BlockIndexEntity")
    long queryMaxHeight();

}
