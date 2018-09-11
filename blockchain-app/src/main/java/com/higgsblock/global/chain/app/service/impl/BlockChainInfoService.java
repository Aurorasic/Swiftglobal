package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgsblock.global.chain.app.dao.IBlockChainInfoRepository;
import com.higgsblock.global.chain.app.dao.entity.BlockChainInfoEntity;
import com.higgsblock.global.chain.app.service.IBlockChainInfoService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-09-06
 */
@Service
public class BlockChainInfoService implements IBlockChainInfoService {

    private static final String KEY_MAX_HEIGHT = "MaxHeight";
    private static final String KEY_ALL_SCORES = "allScores";

    @Autowired
    private IBlockChainInfoRepository blockChainInfoRepository;

    @Override
    public long getMaxHeight() {
        return NumberUtils.toLong(blockChainInfoRepository.findOne(KEY_MAX_HEIGHT).getValue());
    }

    @Override
    public void setMaxHeight(long height) {
        BlockChainInfoEntity entity = new BlockChainInfoEntity(KEY_MAX_HEIGHT, String.valueOf(height));
        blockChainInfoRepository.save(entity);
    }

    @Override
    public Map<String, String> getAllScores() {
        BlockChainInfoEntity chainInfoEntity = blockChainInfoRepository.findOne(KEY_ALL_SCORES);
        if (chainInfoEntity == null ||
                StringUtils.isEmpty(chainInfoEntity.getValue())) {
            return new HashMap<String, String>();
        }
        JSONObject jsonObject = JSONObject.parseObject(chainInfoEntity.getValue());
        Map<String, String> result = (Map) jsonObject;
        return result;
    }

    @Override
    public void setAllScores(Map<String, String> allScores) {
        String scores = JSON.toJSONString(allScores);
        BlockChainInfoEntity entity = new BlockChainInfoEntity(KEY_ALL_SCORES, scores);
        blockChainInfoRepository.save(entity);
    }

    @Override
    public void deleteAllScores() {
        blockChainInfoRepository.delete(KEY_ALL_SCORES);
    }
}
