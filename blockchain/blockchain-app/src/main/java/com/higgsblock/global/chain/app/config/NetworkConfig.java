package com.higgsblock.global.chain.app.config;

import com.higgsblock.global.chain.app.net.api.IRegistryApi;
import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.app.net.config.RegistryConfig;
import com.higgsblock.global.chain.network.config.SocketConfig;
import com.higgsblock.global.chain.network.enums.NetworkType;
import com.higgsblock.global.chain.network.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * network config
 *
 * @author baizhengwen
 * @date 2018/2/24
 */
@Configuration
@Slf4j
public class NetworkConfig {

    @Bean
    public PeerConfig peerConfig(AppConfig config) {
        PeerConfig peerConfig = new PeerConfig();
        peerConfig.setConnectionTimeOutMs(config.getConnectionTimeout());
        peerConfig.setIp(config.getClientPublicIp());
        peerConfig.setSocketPort(config.getSocketServerPort());
        peerConfig.setHttpPort(config.getHttpServerPort());
        peerConfig.setPriKey(config.getPriKey());
        peerConfig.setNetworkType(NetworkType.getNetworkType(config.getNetworkType()));
        return peerConfig;
    }

    @Bean
    public RegistryConfig registryConfig(AppConfig config) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setIp(config.getRegistryCenterIp());
        registryConfig.setPort(config.getRegistryCenterPort());
        return registryConfig;
    }

    @Bean
    public IRegistryApi registryApi(RegistryConfig config) {
        return HttpClient.getApi(config.getIp(), config.getPort(), IRegistryApi.class);
    }

    @Bean
    public SocketConfig socketConfig(AppConfig config) {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setServerPort(config.getSocketServerPort());
        socketConfig.setConnectionTimeOutMs(config.getConnectionTimeout());
        // todo baizhengwen read from config
        socketConfig.setChannelLimitIn(100);
        socketConfig.setChannelLimitOut(100);
        return socketConfig;
    }
}