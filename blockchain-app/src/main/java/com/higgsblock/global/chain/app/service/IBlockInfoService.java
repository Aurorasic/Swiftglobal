package com.higgsblock.global.chain.app.service;

/**
 * @author baizhengwen
 * @date 2018-09-06
 */
public interface IBlockInfoService {
    long getMaxHeight();

    void setMaxHeight(long height);
}
