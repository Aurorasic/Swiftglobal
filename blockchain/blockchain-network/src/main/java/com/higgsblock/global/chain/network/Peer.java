package com.higgsblock.global.chain.network;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The peer node model in the p2p network.
 *
 * @author zhao xiaogang
 */
@Data
@Slf4j
public class Peer extends BaseSerializer {
    /**
     * maximum number of retries a client can connect to a peer.
     */
    private static final int MAXIMUM_RETRIES = 5;

    /**
     * kept for system upgrading in the future.
     */
    private int version;

    /**
     * public key a peer publishes, a corresponding private key will be held by the peer itself.
     */
    private String pubKey;

    /**
     * used when a client connects to a peer.
     */
    private String ip;

    /**
     * used when a client connects to a peer via socket request.
     */
    private int socketServerPort;

    /**
     * used when a client connects to a peer via http request.
     */
    private int httpServerPort;

    /**
     * used to check identity of a peer.
     */
    private String signature;

    /**
     * times a client has attempted to connect to a peer. (add by Kong Yu)
     */
    private int retries;

    @Override
    public int hashCode() {
        return Objects.hashCode(ip, socketServerPort, httpServerPort);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Peer)) {
            return false;
        }
        Peer other = (Peer) o;
        return ip.equals(other.getIp())
                && socketServerPort == other.getSocketServerPort()
                && httpServerPort == other.getHttpServerPort();
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
        if (retries < 0 || retryExceedLimitation()) {
            return false;
        }
        if (!validSignature()) {
            LOGGER.error("Peer signature is incorrect.");
            return false;
        }
        return true;
    }

    public boolean validSignature() {
        return ECKey.verifySign(getHash(), signature, pubKey);
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
        signature = ECKey.signMessage(getHash(), priKey);
    }

    public String getId() {
        return ECKey.pubKey2Base58Address(pubKey);
    }

    public static String getPeersIds(List<Peer> peers) {
        return StringUtils.join(peers.stream().map(peer -> peer.getId()).collect(Collectors.toList()), ",");
    }

    /**
     * Get socket address.
     */
    public String getSocketAddress() {
        return String.format("%s:%d", ip, socketServerPort);
    }

    /**
     * Triggered by failing to connect to this peer.
     */
    public void onTryCompleted() {
        retries++;
    }

    /**
     * Check if number of retry reaches the limitation.
     */
    public boolean retryExceedLimitation() {
        return retries >= MAXIMUM_RETRIES;
    }
}
