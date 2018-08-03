package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "BlockIndex", key = "#blockHash")
    BlockIndexEntity findByBlockHash(String blockHash);

    /**
     * find BlockIndexEntities by height
     *
     * @param height
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    List<BlockIndexEntity> findByHeight(long height);

    /**
     * query BlockIndexEntity records max height
     *
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    @Query(value = "select height from t_block_index order by height desc limit 1", nativeQuery = true)
    long queryMaxHeight();

    /**
     * delete BlockIndexEntities by height
     *
     * @param height
     * @return
     */
    int deleteAllByHeight(long height);

}
