package com.higgsblock.global.chain.network.socket;

import com.higgsblock.global.chain.network.socket.channel.ChannelManager;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-07-23
 */
@Component
@Slf4j
public class MessageSender implements IMessageSender<String> {

    @Autowired
    private MessageCache messageCache;
    @Autowired
    private ChannelManager channelManager;

    @Override
    public boolean unicast(String channelId, String content) {
        try {
            boolean isSuccess = sendMessage(channelId, content);
            LOGGER.info("unicast message: {}, result={}", content, isSuccess);
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean broadcast(String content) {
        return broadcast(null, content);
    }

    @Override
    public boolean broadcast(String[] excludeChannelIds, String content) {
        try {
            List<Channel> channels = channelManager.all();

            //Do not send business message to excluded peers
            for (Channel channel : channels) {
                if (!ArrayUtils.contains(excludeChannelIds, channel.id().toString())) {
                    sendMessage(channel, content);
                }
            }
            LOGGER.info("broadcast message: {}", content);
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Send message with the specific connection.
     */
    private boolean sendMessage(Channel channel, String content) {
        if (null == channel) {
            LOGGER.info("channel is not exist");
            return false;
        }
        String channelId = channel.id().toString();
        if (!messageCache.isCached(channelId, content)) {
            channel.writeAndFlush(content);
            return true;
        }
        return false;
    }

    /**
     * Send message with the specific connection.
     */
    private boolean sendMessage(String channelId, String content) {
        Channel channel = channelManager.find(channelId);
        return sendMessage(channel, content);
    }

}
