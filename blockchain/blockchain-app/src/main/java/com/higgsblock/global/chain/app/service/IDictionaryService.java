package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.LatestBestBlockIndex;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;

/**
 * @author zhao xiaogang
 * @date 2018/5/31
 */
public interface IDictionaryService {

    /**
     * Save latest best block index
     * @param height block height
     * @param bestBlockHash the best block hash
     * @return BaseDaoEntity
     */
    BaseDaoEntity saveLatestBestBlockIndex(long height, String bestBlockHash);

    /**
     * Get latest best block index
     * @return LatestBestBlockIndex
     */
    LatestBestBlockIndex getLatestBestBlockIndex();


}
