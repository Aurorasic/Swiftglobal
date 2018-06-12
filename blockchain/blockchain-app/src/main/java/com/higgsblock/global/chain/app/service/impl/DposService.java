package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.higgsblock.global.chain.app.dao.DposDao;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.service.IDposService;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yangyi
 * @deta 2018/5/24
 * @description
 */
@Service
public class DposService implements IDposService {

    @Autowired
    private DposDao dposDao;

    @Override
    public List<String> get(long sn) {
        try {
            String s = dposDao.get(sn);
            return JSONObject.parseArray(s, String.class);
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get dpos error while the sn is " + sn);
        }
    }

    @Override
    public BaseDaoEntity put(long sn, List<String> addresses) {
        return dposDao.getEntity(sn, JSONObject.toJSONString(addresses));
    }

    @Override
    public List<byte[]> keys() {
        return dposDao.keys();
    }

}
