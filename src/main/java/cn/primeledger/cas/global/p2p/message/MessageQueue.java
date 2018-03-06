package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.p2p.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yuanjiantao
 * @since 2/23/2018
 **/

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class MessageQueue {

    private static final ScheduledExecutorService TIMER = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
        private AtomicInteger cnt = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "msg-" + cnt.getAndIncrement());
        }
    });

    private Queue<MessageWrapper> requestQueue = new ConcurrentLinkedQueue<>();
    private Queue<MessageWrapper> respondQueue = new ConcurrentLinkedQueue<>();
    private ChannelHandlerContext ctx = null;
    private ScheduledFuture<?> timerTask;
    boolean hasPing = false;
    private Channel channel;

    public void activate(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        timerTask = TIMER.scheduleAtFixedRate(() -> {
            try {
                nudgeQueue();
            } catch (Throwable t) {
                LOGGER.error("Unhandled exception", t);
            }
        }, 10, 10, TimeUnit.MILLISECONDS);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void sendMessage(BaseMessage baseMessage) {

        if (baseMessage.getAnswerMessage() != null) {
            requestQueue.add(new MessageWrapper(baseMessage));
        } else {
            respondQueue.add(new MessageWrapper(baseMessage));
        }
    }

    private void disconnect() {
        ctx.close();
    }

    public void receivedMessage(BaseMessage baseMessage) throws InterruptedException {

        if (requestQueue.peek() != null) {
            MessageWrapper messageWrapper = requestQueue.peek();
            BaseMessage waitingMessage = messageWrapper.getBaseMessage();

            if (waitingMessage instanceof PingMessage) {
                hasPing = false;
            }

            if (waitingMessage.getAnswerMessage() != null
                    && baseMessage.getClass() == waitingMessage.getAnswerMessage()) {
                messageWrapper.setAnswered(true);
            }
        }
    }

    private void removeAnsweredMessage(MessageWrapper messageWrapper) {
        if (messageWrapper != null && messageWrapper.isAnswered()) {
            requestQueue.remove();
        }
    }

    private void nudgeQueue() {
        // remove last answered message on the queue
        removeAnsweredMessage(requestQueue.peek());
        // Now send the next message
        sendToWire(respondQueue.poll());
        sendToWire(requestQueue.peek());
    }

    private void sendToWire(MessageWrapper messageWrapper) {

        if (messageWrapper != null && messageWrapper.getRetryTimes() == 0) {
            // TODO: retry logic || hasToRetry()){

            BaseMessage msg = messageWrapper.getBaseMessage();

            ctx.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

            if (msg.getAnswerMessage() != null) {
                messageWrapper.incRetryTimes();
                messageWrapper.saveTime();
            }
        }
    }

    public void close() {
        if (timerTask != null) {
            timerTask.cancel(false);
        }
    }

    public MessageWrapper receive(BaseMessage baseMessage) {
        if (requestQueue.peek() != null) {
            MessageWrapper messageWrapper = requestQueue.peek();
            BaseMessage msg = messageWrapper.getBaseMessage();

            if (msg.getAnswerMessage() != null && baseMessage.getClass() == msg.getAnswerMessage()) {
                messageWrapper.setAnswered(true);
                return messageWrapper;
            }
        }

        return null;
    }


}
