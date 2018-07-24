package com.higgsblock.global.chain.network.socket.channel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.network.config.SocketConfig;
import com.higgsblock.global.chain.network.socket.constants.ChannelType;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-07-23
 */
@Component
public class ChannelManager {

    @Autowired
    private SocketConfig config;

    private Map<String, Channel> inChannelGroup = Maps.newConcurrentMap();
    private Map<String, Channel> outChannelGroup = Maps.newConcurrentMap();

    public boolean add(Channel channel, ChannelType channelType) {
        if (ChannelType.IN == channelType) {
            if (inChannelGroup.size() < config.getChannelLimitIn()) {
                inChannelGroup.put(channel.id().toString(), channel);
                return true;
            }
        } else if (ChannelType.OUT == channelType) {
            if (outChannelGroup.size() < config.getChannelLimitOut()) {
                outChannelGroup.put(channel.id().toString(), channel);
                return true;
            }
        }
        return false;
    }

    public List<Channel> all() {
        LinkedList<Channel> list = Lists.newLinkedList();
        list.addAll(inChannelGroup.values());
        list.addAll(outChannelGroup.values());
        return list;
    }

    public void discard(String channelId) {
        Channel channel = inChannelGroup.remove(channelId);
        if (null != channel) {
            channel.close();
        }

        channel = outChannelGroup.remove(channelId);
        if (null != channel) {
            channel.close();
        }
    }

    public Channel find(String channelId) {
        Channel channel = inChannelGroup.get(channelId);
        if (null != channel) {
            return channel;
        }
        return outChannelGroup.get(channelId);
    }
}
