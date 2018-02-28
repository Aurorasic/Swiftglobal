package cn.primeledger.cas.global.p2p;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;


/**
 * The peer node model in the p2p network.
 *
 * @author zhao xiaogang
 */
public class Peer {
    private InetSocketAddress address;

    private int version;

    public Peer(InetSocketAddress address) {
        this.address = address;
    }

    public Peer(InetAddress ip, int port) {
        this(new InetSocketAddress(ip, port));
    }

    public Peer(String ip, int port) {
        this(new InetSocketAddress(ip, port));
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

    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Peer && address.equals(((Peer) o).getAddress());
    }

    @Override
    public String toString() {
        return getIp() + ":" + getPort();
    }
}
