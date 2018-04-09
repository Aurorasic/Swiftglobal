package cn.primeledger.cas.global.network.socket.connection;

import cn.primeledger.cas.global.network.Peer;
import cn.primeledger.cas.global.network.socket.message.BaseMessage;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.google.common.collect.Queues;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * The connection contains the remote peer basic connection information, the connection state. Connection is controlled by the
 * {@link ConnectionManager} which maintains the connection's count and state.
 *
 * @author zhao xiaogang
 */
@Slf4j
public class Connection {

    @Getter
    private boolean isClient;

    @Getter
    private String id;

    @Getter
    private ChannelHandlerContext context;

    @Getter
    private Peer peer;

    @Getter
    private volatile boolean isActivated = false;

    private ExecutorService queueThreadPool;
    private BlockingQueue<BaseMessage> acceptQueue;
    private BlockingQueue<BaseMessage> sendQueue;

    public Connection(String id, boolean isClient) {
        this.id = id;
        this.isClient = isClient;
    }

    public int getPort() {
        return peer.getSocketServerPort();
    }

    public String getIp() {
        return peer.getIp();
    }

    /**
     * Get the connection id
     */
    public String getPeerId() {
        if (!isActivated()) {
            return null;
        }
        return peer.getId();
    }

    public synchronized boolean acceptMessage(BaseMessage message) {
        if (isActivated) {
            return acceptQueue.offer(message);
        }
        return false;
    }

    public synchronized boolean sendMessage(BaseMessage message) {
        if (isActivated) {
            LOGGER.info("message [{}] will be send by connection [peerId={}]", message, getPeerId());
            return sendQueue.offer(message);
        }
        return false;
    }

    /**
     * Set the connection's state to be true.
     */
    public synchronized void active(Peer peer, ChannelHandlerContext context) {
        if (null == peer || null == context) {
            return;
        }
        this.peer = peer;
        if (isActivated) {
            return;
        }
        this.context = context;
        this.isActivated = true;

        acceptQueue = Queues.newLinkedBlockingQueue();
        sendQueue = Queues.newLinkedBlockingQueue();
        queueThreadPool = ExecutorServices.newFixedThreadPool("connection-queue", 2, 10000);

        queueThreadPool.submit((Runnable) () -> {
            while (true) {
                try {
                    BaseMessage message = sendQueue.take();
                    if (null != message) {
                        context.writeAndFlush(message);
                        LOGGER.info("message [{}] is sent by connection [peerId={}]", message, getPeerId());
                    }
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });

//        queueThreadPool.submit((Runnable) () -> {
//            while (true) {
//                try {
//                    BaseMessage<?> message = responseQueue.take();
//                    Application.EVENT_BUS.post(null);
//                } catch (InterruptedException e) {
//                    LOGGER.error(e.getMessage(), e);
//                }
//            }
//        });

        LOGGER.info("connection is activated, id={}", id);
    }

    public synchronized void close() {
        peer = null;
        if (null != queueThreadPool && !queueThreadPool.isShutdown()) {
            queueThreadPool.shutdown();
            queueThreadPool = null;
        }
        acceptQueue = null;
        sendQueue = null;
        isActivated = false;
        LOGGER.info("connection is closed, id={}", id);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        return builder.append("id", getId())
                .append("peerId", getPeerId())
                .append("ip", getIp())
                .append("port", getPort())
                .append("isClient", isClient())
                .append("isActivated", isActivated())
                .toString();
    }
}
