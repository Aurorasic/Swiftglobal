package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import org.rocksdb.RocksDBException;

import java.util.Map;

/**
 * @author HuangShengli
 * @date 2018-05-23
 */
public interface IScoreService {
    /**
     * get score by address
     *
     * @param address
     * @return
     * @throws RocksDBException
     */
    Integer get(String address) throws RocksDBException;

    /**
     * set score
     *
     * @param address
     * @param score
     * @return
     */
    BaseDaoEntity put(String address, Integer score);

    /**
     * set score if not exist
     *
     * @param address
     * @param score
     * @return
     * @throws RocksDBException
     */
    BaseDaoEntity putIfAbsent(String address, Integer score) throws RocksDBException;

    /**
     * remove score
     *
     * @param address
     * @return
     */
    BaseDaoEntity remove(String address);

    /**
     * load all score
     *
     * @return
     * @throws RocksDBException
     */
    Map<String, Integer> loadAll() throws RocksDBException;

}
