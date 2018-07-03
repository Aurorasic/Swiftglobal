package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import com.higgsblock.global.chain.app.dao.iface.IDposEntity;
import com.higgsblock.global.chain.app.service.IDposService;
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
    private IDposEntity dposDao;

    @Override
    public List<String> get(long sn) {
        DposEntity dposEntity = dposDao.getByField(sn);
        return null == dposEntity ? null : JSONObject.parseArray(dposEntity.getAddresses(), String.class);
    }

    @Override
    public void put(long sn, List<String> addresses) {
        dposDao.add(new DposEntity(sn, JSONObject.toJSONString(addresses)));
    }

}
