package cn.primeledger.cas.global.p2p.listener;

import cn.primeledger.cas.global.common.event.BroadcastEvent;
import cn.primeledger.cas.global.common.event.CollectSignEvent;
import cn.primeledger.cas.global.common.event.UnicastEvent;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.p2p.channel.Channel;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.message.BizMessage;
import cn.primeledger.cas.global.p2p.message.BizWapper;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Slf4j
@Component
public class SendMessageListener implements IEventBusListener {

    @Autowired
    private ChannelMgr channelMgr;

    @Subscribe
    public void process(BroadcastEvent event) {
        LOGGER.info("Accepted broadcast event, type:[{}]", event.getEntity().getType());
        channelMgr.putMessageCached(event.getEntity().getData());

        List<Channel> channelList = channelMgr.getActiveChannels();
        String[] excludeSourceIds = event.getEntity().getExcludeSourceIds();

        //Do not send business message to excluded peers
        if (excludeSourceIds != null && excludeSourceIds.length > 0) {
            List<String> ids = Arrays.asList(excludeSourceIds);
            for (Channel channel : channelList) {
                String cid = String.valueOf(channel.getId());
                if (!ids.contains(cid)) {
                    processEvent(channel, event.getEntity().getType(),
                            event.getEntity().getVersion(),
                            event.getEntity().getData());
                }
            }
        } else {
            for (Channel channel : channelList) {
                processEvent(channel, event.getEntity().getType(),
                        event.getEntity().getVersion(),
                        event.getEntity().getData());
            }
        }
    }

    @Subscribe
    public void process(UnicastEvent event) {
        LOGGER.info("Accepted unique event, type:[{}]", event.getEntity().getType());
        channelMgr.putMessageCached(event.getEntity().getData());

        Long id = Long.parseLong(event.getEntity().getSourceId());
        Channel channel = channelMgr.getChannelById(id);
        processEvent(channel, event.getEntity().getType(),
                event.getEntity().getVersion(),
                event.getEntity().getData());
    }

    @Subscribe
    public void process(CollectSignEvent event) {
        List<Channel> channelList = channelMgr.getActiveChannels();
        for (Channel channel : channelList) {
            if (channel.isDelegate()) {
                processEvent(channel, event.getEntity().getType(),
                        event.getEntity().getVersion(),
                        event.getEntity().getData());
            }
        }
    }

    private void processEvent(Channel channel, short type, short version, String data) {
        BizWapper bizWapper = new BizWapper();
        bizWapper.setType(type);
        bizWapper.setVersion(version);
        bizWapper.setData(data);

        BizMessage bizMessage = new BizMessage(bizWapper);
        channel.getMessageQueue().sendMessage(bizMessage);
    }
}
