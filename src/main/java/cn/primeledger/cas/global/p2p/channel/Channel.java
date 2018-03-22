package cn.primeledger.cas.global.p2p.channel;

import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.message.BaseMessage;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.google.common.collect.Queues;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * The channel contains the remote peer basic connection information, the connection state. Channel is controlled by the
 * {@link ChannelMgr} which maintains the channel's count and state.
 *
 * @author zhao xiaogang
 */
@Slf4j
public class Channel {

    @Getter
    private boolean isInbound;

    @Getter
    private String id;

    @Getter
    private ChannelHandlerContext context;

    @Getter
    private Peer peer;

    @Getter
    private volatile boolean isActivated = false;

    private ExecutorService queueThreadPool;
    private BlockingQueue<BaseMessage<?>> acceptQueue;
    private BlockingQueue<BaseMessage<?>> sendQueue;

    public Channel(String id, boolean isInbound) {
        this.id = id;
        this.isInbound = isInbound;
    }

    public int getPort() {
        return peer.getSocketServerPort();
    }

    public String getIp() {
        return peer.getIp();
    }

    /**
     * Get the channel id
     */
    public String getPeerId() {
        if (!isActivated()) {
            return null;
        }
        return peer.getId();
    }

    public boolean acceptMessage(BaseMessage<?> message) {
        return acceptQueue.offer(message);
    }

    public boolean sendMessage(BaseMessage<?> message) {
        return sendQueue.offer(message);
    }

    /**
     * Set the channel's state to be true.
     */
    public synchronized void onActive(Peer peer, ChannelHandlerContext context) {
        if (isActivated) {
            return;
        }
        this.peer = peer;
        this.context = context;
        this.isActivated = true;

        acceptQueue = Queues.newLinkedBlockingQueue();
        sendQueue = Queues.newLinkedBlockingQueue();
        queueThreadPool = ExecutorServices.newFixedThreadPool("channel-queue", 2, 10000);

        queueThreadPool.submit((Runnable) () -> {
            while (true) {
                try {
                    BaseMessage<?> message = sendQueue.take();
                    if (null != message) {
                        context.writeAndFlush(message);
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
    }

    public synchronized void close() {
        peer = null;
        queueThreadPool.shutdown();
        queueThreadPool = null;
        acceptQueue = null;
        sendQueue = null;
        isActivated = false;
    }

}
