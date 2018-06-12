package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.blockchain.LatestBestBlockIndex;
import com.higgsblock.global.chain.app.dao.LatestBlockIndexDao;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.service.IDictionaryService;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author zhao xiaogang
 * @date 2018/5/31
 */
@Service
public class DictionaryService implements IDictionaryService {
    private static final String LATEST_BEST_BLOCK_INDEX = "latest-best-block-index";

    @Autowired
    private LatestBlockIndexDao latestBlockIndexDao;

    @Override
    public BaseDaoEntity saveLatestBestBlockIndex(long height, String bestBlockHash) {
        LatestBestBlockIndex index = new LatestBestBlockIndex(height, bestBlockHash);
        return latestBlockIndexDao.getEntity(LATEST_BEST_BLOCK_INDEX, index);
    }

    @Override
    public LatestBestBlockIndex getLatestBestBlockIndex() {
        try {
            return latestBlockIndexDao.get(LATEST_BEST_BLOCK_INDEX);
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get latest best block error");
        }
    }

}
