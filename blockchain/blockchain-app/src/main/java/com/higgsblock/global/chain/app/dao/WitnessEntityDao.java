package com.higgsblock.global.chain.app.dao;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.WitnessEntity;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.rocksdb.RocksDBException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuweizhen
 * @date 2018-05-21
 */
@Slf4j
@Repository
public class WitnessEntityDao extends BaseDao<String, WitnessEntity> {

    @Override
    protected String getColumnFamilyName() {
        return "witness";
    }

    /**
     * @return
     */
    public List<WitnessEntity> getByHeight(long height) {
        List<WitnessEntity> entities = allValues();
        if (CollectionUtils.isNotEmpty(entities)) {

            List<WitnessEntity> list = entities.stream().filter(witnessEntity -> {
                return height >= witnessEntity.getHeightStart() && height <= witnessEntity.getHeightEnd();
            }).collect(Collectors.toList());

            return list;
        }

        return new ArrayList<>(0);
    }

    /**
     * @return
     */
    public boolean addAll(List<WitnessEntity> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            List<BaseDaoEntity> list = Lists.newArrayList();
            entities.forEach(witnessEntity -> {
                list.add(new BaseDaoEntity(witnessEntity.getPubKey(), witnessEntity, getColumnFamilyName()));
            });

            try {
                writeBatch(list);
            } catch (RocksDBException e) {
                LOGGER.error("add witness to db failed, entities = {}.", entities, e);
                return false;
            }
        }

        return true;
    }

    /**
     * @return
     */
    public List<WitnessEntity> getAll() {
        return allValues();
    }
}
