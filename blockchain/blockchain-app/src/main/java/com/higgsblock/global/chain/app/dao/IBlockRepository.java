package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IBlockRepository extends JpaRepository<BlockEntity, Long> {

    /**
     * find BlockEntity by blockHash
     *
     * @param blockHash
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    BlockEntity findByBlockHash(String blockHash);


    /**
     * delete BlockEntity by height
     *
     * @param height
     * @return
     */
    int deleteAllByHeight(long height);

}
