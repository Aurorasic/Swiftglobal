package com.higgsblock.global.chain.app.keyvalue.repository;

import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface IKeyValueRepository<T, ID extends Serializable> extends KeyValueRepository<T, ID> {
    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @Override
    List<T> findAll();
}
