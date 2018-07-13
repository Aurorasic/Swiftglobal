package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import com.higgsblock.global.chain.app.dao.iface.IDposRepository;
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
    private IDposRepository dposRepository;

    @Override
    public List<String> get(long sn) {
        DposEntity dposEntity = dposRepository.findBySn(sn);
        return null == dposEntity ? null : JSONObject.parseArray(dposEntity.getAddresses(), String.class);
    }

    @Override
    public void put(long sn, List<String> addresses) {
        dposRepository.save(new DposEntity(sn, JSONObject.toJSONString(addresses)));
    }

}
