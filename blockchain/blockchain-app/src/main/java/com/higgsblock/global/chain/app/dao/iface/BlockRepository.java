package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface BlockRepository extends JpaRepository<BlockEntity, Long> {
}
