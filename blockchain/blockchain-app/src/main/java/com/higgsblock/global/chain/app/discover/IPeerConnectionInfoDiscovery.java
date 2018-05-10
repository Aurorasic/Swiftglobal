package com.higgsblock.global.chain.app.discover;

/**
 * @author baizhengwen
 * @date 2018/4/11
 */
public interface IPeerConnectionInfoDiscovery {

    String getIp();

    int getSocketPort();

    int getHttpPort();
}
