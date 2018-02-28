package cn.primeledger.cas.global.p2p.channel;

import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.message.MessageQueue;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The channel contains the remote peer basic connection information, the connection state. Channel is controlled by the
 * {@link ChannelMgr} which maintains the channel's count and state.
 *
 * @author zhao xiaogang
 */
public class Channel {
    private static final AtomicLong ATOMIC_LONG = new AtomicLong(0);

    private long id;
    private boolean isInbound;
    private boolean isActive;

    private InetSocketAddress peerAddress;
    private MessageQueue messageQueue;

    public Channel(InetSocketAddress peerAddress, boolean isInbound) {
        this.id = ATOMIC_LONG.getAndIncrement();
        this.peerAddress = peerAddress;
        this.isInbound = isInbound;
        this.messageQueue = new MessageQueue();
    }

    /**
     * Get the remote peer's address.
     */
    public InetSocketAddress getPeerAddress() {
        return peerAddress;
    }

    /**
     * Get the the remote peer's port.
     */
    public int getPeerPort() {
        return peerAddress.getPort();
    }

    /**
     * Return the channel's state.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Set the channel's state to be true.
     */
    public void onActive() {
        isActive = true;
    }

    /**
     * Set the channel's state to be false
     */
    public void deactive() {
        isActive = false;
    }

    /**
     * Return if the channel is inbound or outbound.
     */
    public boolean isInbound() {
        return isInbound;
    }

    /**
     * Get the channel id
     */
    public long getId() {
        return id;
    }

    /**
     * Get the channel's remote peer.
     */
    public Peer getPeerNode() {
        return new Peer(peerAddress);
    }

    /**
     * Get the message queue.
     */
    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

}
