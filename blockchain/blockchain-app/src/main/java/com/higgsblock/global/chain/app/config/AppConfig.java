package com.higgsblock.global.chain.app.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * system config
 *
 * @author baizhengwen
 * @create 2017-03-07 19:32
 */

@Getter
@Configuration
@PropertySource(value = "${spring.config.location}", name = "appConf")
public class AppConfig {

    @Autowired
    private Environment environment;

    @Value("${data.root.path}")
    private String rootDataPath;

    @Value("${data.blockchain.path}")
    private String blockChainDataPath;

    @Value("${data.blockchain.file}")
    private String blockChainDataFile;

    @Value("${registry.center.ip}")
    private String registryCenterIp;

    @Value("${registry.center.port}")
    private int registryCenterPort;

    @Value("${peer.priKey}")
    private String priKey;

    @Value("${server.port}")
    private int httpServerPort;

    @Value("${p2p.maxOutboundConnections}")
    private int maxOutboundConnections;

    @Value("${p2p.maxInboundConnections}")
    private int maxInboundConnections;

    @Value("${p2p.serverListeningPort}")
    private int socketServerPort;

    @Value("${p2p.connectionTimeout}")
    private int connectionTimeout;

    @Value("${p2p.networkType}")
    private byte networkType;

    @Deprecated
    @Value("${p2p.clientPublicIp}")
    private String clientPublicIp;

    @Value("${genesis.block.hash}")
    private String genesisBlockHash;

    @Value("${access.isAllowed}")
    private boolean accessIsAllowed;

    @Value("${access.allow.ip}")
    private String accessAllowIp;

    @Value("${access.allow.ip.range}")
    private String accessAllowIpRange;

    @Value("${access.allow.ip.wild.card}")
    private String accessAllowIpWildCard;

    /**
     * confirm bestchain with a constant number when sporked
     *  add by Huangsheng li 2018-06-29
     */
    @Value("${bestchain.confirm.min.block.num}")
    private int bestchainConfirmNum;
    @Value("${dpos.blocks.per.round}")
    private int dposBlocksPerRound;

    public String getValue(String key) {
        return environment.getProperty(key);
    }

    public <T> T getValue(String key, Class<T> clazz) {
        return environment.getProperty(key, clazz);
    }
}
