package com.higgsblock.global.chain.network.socket.connection;

import com.google.common.collect.Queues;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.socket.message.BaseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * A connection object stores the basic information and state of a connection between this node and another remote node.
 *
 * @author chenjiawei
 * @date 2018-05-22
 */
@Slf4j
public class Connection {
    /**
     * Keep connection not deletable within the timeout after setting context.
     */
    private static final long WAIT_PEER_TIMEOUT = 30 * 1000;


    /**
     * Described with channel id.
     */
    @Getter
    private String id;

    /**
     * The remote node.
     */
    @Getter
    private Peer peer;

    /**
     * The role this node plays in the connection.
     */
    @Getter
    private boolean isClient;

    /**
     * The time at which this connection object is created.
     */
    @Getter
    private long createdTime;

    /**
     * The time at which this connection start to wait peer information.
     */
    private long waitPeerStartTime;


    /**
     * Channel between this node and remote node.
     */
    private NioSocketChannel channel;

    /**
     * The context of channel handler, via which message can be sent and received.
     */
    @Getter
    private ChannelHandlerContext context;

    /**
     * Level of connection.
     */
    @Getter
    @Setter
    private ConnectionLevelEnum connectionLevel;

    /**
     * Queue of message to send to the remote node.
     */
    private BlockingQueue<BaseMessage> sendQueue;

    /**
     * Service for message sending.
     */
    private ExecutorService messageSender;

    /**
     * Used to control stopping of thread pool.
     */
    private volatile boolean stop = false;

    public Connection(NioSocketChannel channel, Peer peer, boolean isClient) {
        this(channel, isClient);
        this.peer = peer;
    }

    public Connection(NioSocketChannel channel, boolean isClient) {
        this.id = channel.id().toString();
        this.channel = channel;
        this.isClient = isClient;
        this.createdTime = System.currentTimeMillis();
    }

    /**
     * Check if connection does not receive peer information within timeout or not.
     */
    public boolean waitPeerTimeout() {
        return isWaitPeer() && (System.currentTimeMillis() - waitPeerStartTime > WAIT_PEER_TIMEOUT);
    }

    private boolean isWaitPeer() {
        return context != null && peer == null;
    }

    /**
     * Check if connection is activated. The word "activated" means that this node can send
     * or receive message via this connection.
     */
    public boolean isActivated() {
        return context != null && peer != null && sendQueue != null;
    }

    /**
     * Release resource allocated to connection.
     */
    public synchronized boolean close() {
        if (messageSender != null && !messageSender.isShutdown()) {
            stop = true;
            messageSender.shutdownNow();
            messageSender = null;
        }
        sendQueue = null;
        if (channel != null) {
            channel.close();
            channel = null;
        }
        context = null;

        LOGGER.info("Connection is closed, id={}", id);
        return true;
    }

    /**
     * Allocate handler context to connection.
     *
     * @param context handler context
     */
    public void setContext(ChannelHandlerContext context) {
        if (this.context != null) {
            return;
        }
        this.context = context;
        this.waitPeerStartTime = System.currentTimeMillis();

        activate();
    }

    private void activate() {
        if (context == null || peer == null) {
            return;
        }

        sendQueue = Queues.newLinkedBlockingQueue();
        messageSender = ExecutorServices.newFixedThreadPool("connection-queue", 2, 10000);

        messageSender.submit(() -> {
            while (!stop) {
                try {
                    BaseMessage message = sendQueue.take();
                    if (message != null) {
                        context.writeAndFlush(message);
                        LOGGER.info("Message [{}] is sent by connection [peerId={}, connectionId={}]", message, getPeerId(), id);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });
    }

    public void setPeer(Peer peer) {
        if (this.peer != null) {
            return;
        }
        this.peer = peer;

        activate();
    }

    /**
     * Get ip of remote node.
     */
    public String getIp() {
        return peer == null ? null : peer.getIp();
    }

    /**
     * Get port of remote node.
     */
    public int getPort() {
        return peer == null ? 0 : peer.getSocketServerPort();
    }

    /**
     * Get id of peer if exists.
     */
    public String getPeerId() {
        return peer == null ? null : peer.getId();
    }

    /**
     * Put the message to send into cache.
     *
     * @param message message to send
     * @return {@code true} if the element was added to this queue, else {@code false}
     */
    public synchronized boolean sendMessage(BaseMessage message) {
        if (isActivated()) {
            LOGGER.info("Message [{}] will be sent by connection [peerId={}]", message, getPeerId());
            return sendQueue.offer(message);
        }
        return false;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        return builder.append("id", id)
                .append("peerId", getPeerId())
                .append("ip", getIp())
                .append("port", getPort())
                .append("isClient", isClient)
                .append("isActivated", isActivated())
                .append("connectionLevel", connectionLevel)
                .toString();
    }
}
