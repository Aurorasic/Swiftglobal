package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.crypto.ECKey;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * The peer node model in the p2p network.
 *
 * @author zhao xiaogang
 */

@Data
public class Peer implements Serializable {

    private int version;

    private String pubKey;

    private String ip;

    private int socketServerPort;

    private int httpServerPort;

    private String signature;

    @Override
    public boolean equals(Object o) {
        return o instanceof Peer && ip.equals(((Peer) o).getIp())
                && (socketServerPort == ((Peer) o).getSocketServerPort())
                && (httpServerPort == ((Peer) o).getHttpServerPort())
                ;
    }

    public boolean validParams() {
        return StringUtils.isNotEmpty(ip)
                && socketServerPort != 0
                && httpServerPort != 0
                && StringUtils.isNotEmpty(pubKey)
                && StringUtils.isNotEmpty(signature);
    }

    public boolean validSignature() {
        return ECKey.verifySign(ip, signature, pubKey);
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
}
