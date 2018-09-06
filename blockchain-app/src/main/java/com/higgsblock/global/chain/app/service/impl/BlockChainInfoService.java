package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.dao.IBlockChainInfoRepository;
import com.higgsblock.global.chain.app.dao.entity.BlockChainInfoEntity;
import com.higgsblock.global.chain.app.service.IBlockChainInfoService;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author baizhengwen
 * @date 2018-09-06
 */
@Service
public class BlockChainInfoService implements IBlockChainInfoService {

    private static final String KEY_MAX_HEIGHT = "MaxHeight";

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
}
