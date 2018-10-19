package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.ContractSenderEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;

import java.util.List;

/**
 * DAO of mappings from owners of transaction inputs to sender of contract.
 *
 * @author Chen Jiawei
 * @date 2018-10-18
 */
public interface IContractSenderRepository extends IKeyValueRepository<ContractSenderEntity, String> {
    /**
     * Saves a mapping.
     *
     * @param entity target mapping.
     * @return stored mapping.
     */
    @Override
    ContractSenderEntity save(ContractSenderEntity entity);

    /**
     * Queries mappings.
     *
     * @param sender contract mapping sender.
     * @return list of mappings.
     */
    @IndexQuery("sender")
    List<ContractSenderEntity> findBySender(String sender);

    /**
     * Deletes contract inputs-sender mapping.
     *
     * @param sender contract mapping sender.
     * @return deleted number.
     */
    @IndexQuery("sender")
    int deleteBySender(String sender);
}
