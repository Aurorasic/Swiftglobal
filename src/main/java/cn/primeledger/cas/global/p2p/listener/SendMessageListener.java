package cn.primeledger.cas.global.p2p.listener;

import cn.primeledger.cas.global.common.event.BroadcastEvent;
import cn.primeledger.cas.global.common.event.UnicastEvent;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.p2p.channel.Channel;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.message.BizMessage;
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

//    @Autowired
//    private PeerMgr peerMgr;

//    @Autowired
//    private AppConfig appConfig;
//    private List<String> addressList;
//    private long nonce;

//    private Cache<Long, CachedEvent> cache = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE)
//            .build();

    @Subscribe
    public void process(BroadcastEvent event) {
        LOGGER.info("Accepted broadcast event, type:[{}]", event.getEntity().getType());

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
                    channelMgr.putMessageCached(event.getEntity().getData(), cid);
                }
            }
        } else {
            for (Channel channel : channelList) {
                processEvent(channel, event.getEntity().getType(),
                        event.getEntity().getVersion(),
                        event.getEntity().getData());

                channelMgr.putMessageCached(event.getEntity().getData(), String.valueOf(channel.getId()));
            }
        }
    }

    @Subscribe
    public void process(UnicastEvent event) {
        LOGGER.info("Accepted unique event, type:[{}]", event.getEntity().getType());
        channelMgr.putMessageCached(event.getEntity().getData(), event.getEntity().getSourceId());

        String peerAddress = event.getEntity().getSourceId();
        Channel channel = channelMgr.getChannelByPeerId(peerAddress);

        if (channel == null) {
            LOGGER.warn("Channel not found");
            return;
        }

        processEvent(channel, event.getEntity().getType(),
                event.getEntity().getVersion(),
                event.getEntity().getData());
    }

//    @Subscribe
//    public void process(CollectSignEvent event) {
//        String[] addressArr = event.getEntity().getIncludeSourceIds();
//        if (addressArr == null || addressArr.length == 0) {
//            return;
//        }
//
//        List<String> addressList = Arrays.asList(addressArr);
//        List<String> existAddressList = new LinkedList<>();
//        channelMgr.getActiveChannels().stream().forEach(channel -> {
//            if (addressList.contains(channel.getId())) {
//                existAddressList.add(channel.getId());
//
//                processEvent(channel, event.getEntity().getType(),
//                        event.getEntity().getVersion(),
//                        event.getEntity().getData());
//            }
//        });
//
//        List<String> connAddressList = addressList.stream()
//                .filter(addr -> !existAddressList.contains(addr))
//                .collect(Collectors.toList());
//
//        if (existAddressList.size() < addressList.size()) {
//            Peer registryPeer = new Peer();
//            registryPeer.setIp(appConfig.getRegistryCenterIp());
//            registryPeer.setHttpServerPort(appConfig.getRegistryCenterPort());
//            peerMgr.addPeer(registryPeer);//connect registry
//        }
//
//        this.addressList = connAddressList;
//        this.nonce = event.getEntity().getHeight();
//        CachedEvent cachedEvent = new CachedEvent();
//        cachedEvent.setAddressList(connAddressList);
//        cachedEvent.setEvent(event);
//        cache.put(event.getEntity().getHeight(), cachedEvent);
//    }

//    @Subscribe
//    public void sendGetAddressMsg(SendGetAddressEvent addressEvent) {
//        Channel channel = channelMgr.getChannelById(addressEvent.getId());
//        if (channel == null) {
//            LOGGER.warn("Channel not found");
//            return;
//        }
//
//        if (CollectionUtils.isEmpty(addressList)) {
//            LOGGER.warn("addressList is null");
//            return;
//        }
//
//
////        CachedEvent cachedEvent = cache.getIfPresent(addressEvent.getNonce());
////        if (cachedEvent == null) {
////            LOGGER.error("Cached event is null");
////            return;
////        }
//
//        GetAddressMessage.Wrapper wrapper = new GetAddressMessage.Wrapper();
//        wrapper.setAddressList(addressList);
//        wrapper.setNonce(this.nonce);
//
//        GetAddressMessage message = new GetAddressMessage(wrapper);
//        channel.getMessageQueue().sendMessage(message);//get address list from registry
//
//        LOGGER.info("get address list from registry ");
//    }

//    @Subscribe
//    public void sendGetSignMsg(SendGetSignEvent signEvent) {
//        Channel channel = channelMgr.getChannelById(signEvent.getId());
//        if (channel == null) {
//            LOGGER.warn("Channel not found");
//            return;
//        }
//
//        CachedEvent cachedEvent = cache.getIfPresent(signEvent.getNonce());
//        if (cachedEvent == null) {
//            LOGGER.error("Cached event is null");
//            return;
//        }
//
//        CollectSignEvent event = cachedEvent.getEvent();
//        processEvent(channel, event.getEntity().getType(),
//                event.getEntity().getVersion(),
//                event.getEntity().getData());
//    }

    private void processEvent(Channel channel, short type, short version, String data) {
        BizMessage.Wrapper wrapper = new BizMessage.Wrapper();
        wrapper.setType(type);
        wrapper.setVersion(version);
        wrapper.setData(data);

        BizMessage bizMessage = new BizMessage(wrapper);
        channel.sendMessage(bizMessage);
    }

//    public CachedEvent getCachedEvent(Long nonce) {
//        return cache.getIfPresent(nonce);
//    }
//
//    @Data
//    public static class CachedEvent {
//        private List<String> addressList;
//        private CollectSignEvent event;
//    }
}
