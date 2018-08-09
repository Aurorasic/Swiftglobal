package com.higgsblock.global.chain.app.net.listener;

import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.net.connection.ConnectionManager;
import com.higgsblock.global.chain.app.net.message.Hello;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.network.socket.constants.ChannelType;
import com.higgsblock.global.chain.network.socket.event.CreateChannelEvent;
import com.higgsblock.global.chain.network.socket.event.DiscardChannelEvent;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
@Slf4j
@Component
public class ChannelChangedListener implements IEventBusListener {

    @Autowired
    private MessageCenter messageCenter;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private ConnectionManager connectionManager;

    @Subscribe
    public void process(CreateChannelEvent event) {
        Channel channel = event.getChannel();
        ChannelType channelType = event.getType();
        LOGGER.info("CreateChannelEvent: channelId={}, type={}", channel.id(), channelType);
        connectionManager.createConnection(channel, channelType);

        Hello hello = new Hello();
        hello.setPeer(peerManager.getSelf());

        String channelId = channel.id().toString();
        messageCenter.handshake(channelId, hello);
    }

    @Subscribe
    public void process(DiscardChannelEvent event) {
        String channelId = event.getChannelId();
        LOGGER.info("DiscardChannelEvent: channelId={}", channelId);
        connectionManager.closeByChannelId(channelId);
    }
}
