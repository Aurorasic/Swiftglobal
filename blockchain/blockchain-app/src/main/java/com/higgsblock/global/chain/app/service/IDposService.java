package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;

import java.util.List;

/**
 * @author yangyi
 * @deta 2018/5/24
 * @description
 */
public interface IDposService {

    /**
     * get dpos addresses by serial number
     * @param sn the serial number
     * @return the dpos address list
     * */
    List<String> get(long sn);

    /**
     * put the dpos addresses into local database while the key is sn
     *@param sn the key of the dpos addresses in local database
     *@param addresses the dpos address list
     * */
    BaseDaoEntity put(long sn, List<String> addresses);

    /**
     * get all dpos addresses key
     * */
    List<byte[]> keys();
}
