package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockChainInfoEntity;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IBlockChainInfoRepository extends IKeyValueRepository<BlockChainInfoEntity, String> {

    @Override
    BlockChainInfoEntity save(BlockChainInfoEntity entity);

    @Override
    BlockChainInfoEntity findOne(String id);
}
