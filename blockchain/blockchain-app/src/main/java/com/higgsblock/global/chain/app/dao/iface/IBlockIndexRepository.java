package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IBlockIndexRepository extends JpaRepository<BlockIndexEntity, Long> {

    BlockIndexEntity findByBlockHash(String blockHash);

    List<BlockIndexEntity> findAllByHeight(long height);

    @Query("select max (height) from BlockIndexEntity")
    long queryMaxHeight();

}
