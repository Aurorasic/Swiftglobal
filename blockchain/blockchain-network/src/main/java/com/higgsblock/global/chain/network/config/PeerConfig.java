package com.higgsblock.global.chain.network.config;

import com.higgsblock.global.chain.crypto.ECKey;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * @author baizhengwen
 * @date 2018-04-28
 */
@Data
public class PeerConfig {

    private int connectionTimeOutMs;
    private String ip;
    @Deprecated
    private int httpPort;
    private int socketPort;
    private String priKey;

    public String getPubKey() {
        if (StringUtils.isNotBlank(priKey)) {
            return ECKey.fromPrivateKey(priKey).getKeyPair().getPubKey();
        }
        return null;
    }

    public String getAddress() {
        if (StringUtils.isNotBlank(priKey)) {
            return ECKey.fromPrivateKey(priKey).toBase58Address();
        }
        return null;
    }
}
