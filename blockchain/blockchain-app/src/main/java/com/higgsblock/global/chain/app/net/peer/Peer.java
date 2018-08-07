package com.higgsblock.global.chain.app.net.peer;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * The peer node model in the p2p network.
 *
 * @author zhao xiaogang
 */
@Data
public class Peer extends BaseSerializer {

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

    @Override
    public int hashCode() {
        return Objects.hashCode(pubKey, ip, socketServerPort, httpServerPort);
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
        return StringUtils.equals(pubKey, other.getPubKey())
                && StringUtils.equals(ip, other.getIp())
                && socketServerPort == other.getSocketServerPort()
                && httpServerPort == other.getHttpServerPort();
    }

    public boolean valid() {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        if (socketServerPort <= 0) {
            return false;
        }
        if (httpServerPort <= 0) {
            return false;
        }
        if (isEqualsPort()) {
            return false;
        }
        if (StringUtils.isEmpty(pubKey)) {
            return false;
        }
        if (StringUtils.isEmpty(signature)) {
            return false;
        }
        if (!validSignature()) {
            return false;
        }
        return true;
    }

    public boolean isEqualsPort() {
        if (this.httpServerPort == this.socketServerPort) {
            return true;
        }
        return false;
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
                .hash().toString();
    }

    public void signature(String priKey) {
        signature = ECKey.signMessage(getHash(), priKey);
    }

    public String getId() {
        return ECKey.pubKey2Base58Address(pubKey);
    }
}
