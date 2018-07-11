package com.higgsblock.global.chain.app.discover;

/**
 * The interface Peer connection info discovery.
 *
 * @author baizhengwen
 * @date 2018 /4/11
 */
public interface IPeerConnectionInfoDiscovery {

    /**
     * Gets ip.
     *
     * @return the ip
     */
    String getIp();

    /**
     * Gets socket port.
     *
     * @return the socket port
     */
    int getSocketPort();

    /**
     * Gets http port.
     *
     * @return the http port
     */
    int getHttpPort();
}