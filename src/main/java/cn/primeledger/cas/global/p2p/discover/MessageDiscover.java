package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.p2p.channel.Channel;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.message.GetPeersMessage;

import java.util.List;

/**
 * Message discover do responses for getting peers when setups p2p connection with peers.
 * Which sends {@link cn.primeledger.cas.global.p2p.message.GetPeersMessage} to the peer
 * nodes, the peer will answer by the {@link cn.primeledger.cas.global.p2p.message.PeersMessage}
 * after a simple invalidation.
 *
 * @author  zhao xiaogang
 */

public class MessageDiscover implements Runnable {
    private ChannelMgr channelMgr;

    public MessageDiscover(ChannelMgr channelMgr) {
        this.channelMgr = channelMgr;
    }

    @Override
    public void run() {
        sendGetPeersMessage();
    }

    private void sendGetPeersMessage() {
        List<Channel> channels =  channelMgr.getActiveChannels();

        for (Channel channel : channels) {
            channel.getMessageQueue().sendMessage(new GetPeersMessage());
        }
    }
}


