package com.higgsblock.global.chain.app.net.connection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.higgsblock.global.chain.app.net.constants.ConnectionLevelEnum;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.network.socket.constants.ChannelType;
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

    @Getter
    private String channelId;

    @Getter
    private String peerId;

    @Getter
    private String ip;

    @Getter
    private int port;

    /**
     * The remote node.
     */
    @Getter
    private Peer peer;

    /**
     * Mark the connection is in or out.
     */
    @Getter
    private ChannelType type;

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

    private Connection(Channel channel, ChannelType type) {
        this.channel = channel;
        if (null != channel) {
            channelId = channel.id().toString();
        }
        this.type = type;
        this.createdTime = System.currentTimeMillis();
        this.connectionLevel = ConnectionLevelEnum.L3;
        this.sendQueue = Queues.newLinkedBlockingQueue();
    }

    public static Connection newInstance(Channel channel, ChannelType type) {
        return new Connection(channel, type);
    }

    public long getAge() {
        return System.currentTimeMillis() - createdTime;
    }

    /**
     * Check if connection handshake timeout or not.
     */
    public boolean isHandshakeTimeOut() {
        return !isActivated && (getAge() > HANDSHAKE_TIMEOUT);
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
        try {
            isActivated = false;
            if (messageSender != null && !messageSender.isShutdown()) {
                messageSender.shutdownNow();
                messageSender = null;
            }
            if (channel != null) {
                channel.close();
            }
            LOGGER.info("Connection is closed, channelId={}, peerId={}", channelId, peerId);
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    public synchronized void activate(Peer peer) {
        if (isActivated || null == channel) {
            return;
        }
        if (peer == null) {
            return;
        }

        this.peer = peer;
        this.peerId = peer.getId();
        this.ip = peer.getIp();
        this.port = peer.getSocketServerPort();
        isActivated = true;
        messageSender = ExecutorServices.newSingleThreadExecutor(String.format("connection-queue-%s-", peerId), 10000);

        messageSender.submit(() -> {
            String message = null;
            while (isActivated) {
                try {
                    message = sendQueue.take();
                    doSend(message);
                    LOGGER.debug("Message [{}] is sent success, channelId={}, peerId={}", message, channelId, peerId);
                } catch (Exception e) {
                    LOGGER.error(String.format("send message error, %s, channelId=%s, peerId=%s", e.getMessage(), channelId, peerId), e);
                    isActivated = false;
                }
            }
        });
        LOGGER.info("Connection is activated, channelId={}, peerId={}", channelId, peerId);
    }

    /**
     * Put the message to send into cache.
     *
     * @param message message to send
     * @return {@code true} if the element was added to this queue, else {@code false}
     */
    public synchronized boolean sendMessage(String message) {
        if (isActivated() && StringUtils.isNotBlank(message)) {
            LOGGER.debug("Message [{}] will be sent, channelId={}, peerId={}", message, channelId, peerId);
            return sendQueue.offer(message);
        }
        return false;
    }

    public synchronized boolean handshake(String message) {
        return doSend(message);
    }

    protected synchronized boolean doSend(String message) {
        Preconditions.checkNotNull(channel, "channel is null, channelId=%s, peerId=%s", channelId, peerId);

        if (StringUtils.isNotBlank(message) && channel.isActive()) {
            channel.writeAndFlush(message);
            LOGGER.debug("Message [{}] is sent success, channelId={}, peerId={}", message, channelId, peerId);
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
                .append("type", type)
                .append("isActivated", isActivated())
                .append("connectionLevel", connectionLevel)
                .toString();
    }
}
