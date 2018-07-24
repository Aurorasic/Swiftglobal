package com.higgsblock.global.chain.network.socket.connection;

import com.google.common.collect.Queues;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.network.Peer;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
     * Keep connection not deletable within the timeout after created.
     */
    private static final long HANDSHAKE_TIMEOUT = 30 * 1000;

    @Getter
    private Channel channel;

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
     * Level of connection.
     */
    @Getter
    @Setter
    private ConnectionLevelEnum connectionLevel;

    /**
     * Queue of message to send to the remote node.
     */
    private BlockingQueue<String> sendQueue;

    /**
     * Service for message sending.
     */
    private ExecutorService messageSender;

    /**
     * Used to control connection status.
     */
    private volatile boolean isActivated;

    public Connection(Channel channel, boolean isClient) {
        this.channel = channel;
        this.isClient = isClient;
        this.createdTime = System.currentTimeMillis();
        this.connectionLevel = ConnectionLevelEnum.L3;
    }

    /**
     * Check if connection handshake timeout or not.
     */
    public boolean isHandshakeTimeOut() {
        return !isActivated && (System.currentTimeMillis() - createdTime > HANDSHAKE_TIMEOUT);
    }

    /**
     * Check if connection is activated. The word "activated" means that this node can send
     * or receive message via this connection.
     */
    public synchronized boolean isActivated() {
        return isActivated;
    }

    /**
     * Release resource allocated to connection.
     */
    public synchronized boolean close() {
        String channelId = getChannelId();
        String peerId = getPeerId();
        try {
            isActivated = false;
            if (messageSender != null && !messageSender.isShutdown()) {
                messageSender.shutdownNow();
                messageSender = null;
            }
            sendQueue = null;
            if (channel != null) {
                channel.close();
                channel = null;
            }
            peer = null;
            LOGGER.info("Connection is closed, channelId={}, peerId={}", channelId, peerId);
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    public synchronized void activate(Peer peer) {
        if (peer == null) {
            return;
        }

        this.peer = peer;

        if (isActivated) {
            return;
        }
        isActivated = true;
        sendQueue = Queues.newLinkedBlockingQueue();
        messageSender = ExecutorServices.newFixedThreadPool("connection-queue", 2, 10000);

        messageSender.submit(() -> {
            while (isActivated) {
                try {
                    String message = sendQueue.take();
                    channel.writeAndFlush(message);
                    LOGGER.info("Message [{}] is sent success, channelId={}, peerId={}", message, getChannelId(), getPeerId());
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });
        LOGGER.info("Connection is activated, channelId={}, peerId={}", getChannelId(), getPeerId());
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

    public String getChannelId() {
        return channel.id().toString();
    }

    /**
     * Put the message to send into cache.
     *
     * @param message message to send
     * @return {@code true} if the element was added to this queue, else {@code false}
     */
    public synchronized boolean sendMessage(String message) {
        if (isActivated() && StringUtils.isNotBlank(message)) {
            LOGGER.info("Message [{}] will be sent, channelId={}, peerId={}", message, getChannelId(), getPeerId());
            return sendQueue.offer(message);
        }
        return false;
    }

    public synchronized boolean handshake(String message) {
        if (StringUtils.isNotBlank(message)) {
            channel.writeAndFlush(message);
            LOGGER.info("Message [{}] is sent success, channelId={}, peerId={}", message, getChannelId(), getPeerId());
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        return builder.append("channelId", getChannelId())
                .append("peerId", getPeerId())
                .append("ip", getIp())
                .append("port", getPort())
                .append("isClient", isClient)
                .append("isActivated", isActivated())
                .append("connectionLevel", connectionLevel)
                .toString();
    }
}
