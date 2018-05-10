package com.higgsblock.global.chain.network;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * The peer node model in the p2p network.
 *
 * @author zhao xiaogang
 */

@Data
@Slf4j
public class Peer extends BaseSerializer {
    private final static int RETRIES = 5;

    private int version;

    private String pubKey;

    private String ip;

    private int socketServerPort;

    private int httpServerPort;

    private String signature;

    /**
     * add by kongyu
     * attempts to connect failed times
     */
    private int retries;

    @Override
    public boolean equals(Object o) {
        return o instanceof Peer && ip.equals(((Peer) o).getIp())
                && (socketServerPort == ((Peer) o).getSocketServerPort())
                && (httpServerPort == ((Peer) o).getHttpServerPort())
                ;
    }

    public boolean valid() {
        if (version < 0) {
            return false;
        }
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        if (socketServerPort == 0) {
            return false;
        }
        if (httpServerPort == 0) {
            return false;
        }
        if (StringUtils.isEmpty(pubKey)) {
            return false;
        }
        if (StringUtils.isEmpty(signature)) {
            return false;
        }
        if (!validSignature()) {
            LOGGER.error("Peer signature is error ");
            return false;
        }
        if (!(retries >= 0 && retries <= 5)) {
            return false;
        }
        return true;
    }

    public String getHash() {
        return Hashing.sha256().newHasher()
                .putString(ip, Charsets.UTF_8)
                .putInt(socketServerPort)
                .putInt(httpServerPort)
                .putString(pubKey, Charsets.UTF_8)
                .putInt(version)
                .hash().toString();
    }

    public void signature(String priKey) {
        this.signature = ECKey.signMessage(getHash(), priKey);
    }

    public boolean validSignature() {
        return ECKey.verifySign(getHash(), signature, pubKey);
    }

    public String getSocketAddress() {
        return String.format("%s:%s", ip, socketServerPort);
    }

    public String getHttpAddress() {
        return String.format("%s:%s", ip, httpServerPort);
    }

    public String getId() {
        return ECKey.pubKey2Base58Address(pubKey);
    }

    public static String getPeersIds(List<Peer> peers) {
        if (CollectionUtils.isEmpty(peers)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Peer peer : peers) {
            sb.append(peer.getId()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
