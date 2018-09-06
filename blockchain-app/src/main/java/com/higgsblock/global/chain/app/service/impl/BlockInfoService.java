package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.dao.IBlockInfoRepository;
import com.higgsblock.global.chain.app.dao.entity.DictionaryEntity;
import com.higgsblock.global.chain.app.service.IBlockInfoService;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author baizhengwen
 * @date 2018-09-06
 */
@Service
public class BlockInfoService implements IBlockInfoService {

    private static final String KEY_MAX_HEIGHT = "MaxHeight";

    @Autowired
    private IBlockInfoRepository blockInfoRepository;

    @Override
    public long getMaxHeight() {
        return NumberUtils.toLong(blockInfoRepository.findOne(KEY_MAX_HEIGHT).getValue());
    }

    @Override
    public void setMaxHeight(long height) {
        DictionaryEntity entity = new DictionaryEntity(KEY_MAX_HEIGHT, String.valueOf(height));
        blockInfoRepository.save(entity);
    }
}
