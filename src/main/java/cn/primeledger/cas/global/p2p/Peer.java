package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.p2p.message.HelloAckWraper;
import cn.primeledger.cas.global.p2p.message.HelloWraper;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;


/**
 * The peer node model in the p2p network.
 *
 * @author zhao xiaogang
 */

@Getter
@Setter
@AllArgsConstructor
public class Peer implements Serializable {

    private InetSocketAddress address;

    private int version;

    private boolean isDelegate;

    public Peer() {
    }

    public Peer(InetSocketAddress address) {
        this.address = address;
    }

    public Peer(InetAddress ip, int port) {
        this(new InetSocketAddress(ip, port));
    }

    public Peer(String ip, int port) {
        this(new InetSocketAddress(ip, port));
    }

    public Peer(String ip, int port, int version) {
        this(new InetSocketAddress(ip, port));
        this.version = version;
    }

    public Peer(byte[] address, byte[] data) {
        byte[] portBytes = Arrays.copyOfRange(data, 0, 4);
        int port = Ints.fromByteArray(portBytes);
    }

    public byte[] toBytes() {
        byte[] portBytes = Ints.toByteArray(getPort());
        portBytes = Bytes.ensureCapacity(portBytes, 4, 0);

        return portBytes;
    }

    public String getIp() {
        return address.getAddress().getHostAddress();
    }

    public int getPort() {
        return address.getPort();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Peer && address.equals(((Peer) o).getAddress());
    }

    @Override
    public String toString() {
        return getIp() + ":" + getPort();
    }

    public static Peer getFromHelloWrapper(HelloWraper helloWraper) {
        return new Peer(helloWraper.getIp(), helloWraper.getPort());
    }

    public static Peer getFromHelloAckWrapper(HelloAckWraper helloWraper) {
        return new Peer(helloWraper.getIp(), helloWraper.getPort());
    }
}
