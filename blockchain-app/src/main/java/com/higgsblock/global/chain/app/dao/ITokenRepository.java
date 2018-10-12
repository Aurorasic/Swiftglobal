package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.ContractEntity;
import com.higgsblock.global.chain.app.dao.entity.TokenEntity;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;
import jdk.nashorn.internal.parser.Token;

/**
 * The interface Contract repository.
 *
 * @author zhao xiaogang
 * @date 2018-10-12
 */
public interface ITokenRepository extends IKeyValueRepository<TokenEntity, String> {
}