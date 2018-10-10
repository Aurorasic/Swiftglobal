package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.dao.entity.ContractEntity;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;

/**
 * The interface Contract repository.
 *
 * @author zhao xiaogang
 * @date 2018-10-10
 */
public interface IContractRepository extends IKeyValueRepository<ContractEntity, String> {
}