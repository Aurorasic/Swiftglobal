package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;

import java.util.Map;

/**
 * The interface Balance repository.
 *
 * @author yanghuadong
 * @date 2018 -09-26
 */
public interface IBalanceRepository extends IKeyValueRepository<BalanceEntity, String> {
}