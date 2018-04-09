package cn.primeledger.cas.global.network;

import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.service.PeerReqService;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * The peer manager provides basic methods to add peers into database or to find peers from database.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class PeerManager implements InitializingBean {

    @Autowired
    private ConcurrentMap<String, Peer> peerMap;
    @Autowired
    private PeerReqService peerReqService;
    @Autowired
    private AppConfig config;

    private Peer self;

    @Override
    public void afterPropertiesSet() throws Exception {
        getSelf();
    }

    /**
     * Add peers to the peer queue.
     */
    public void add(Collection<Peer> collection) {
        CollectionUtils.forAllDo(collection, o -> add((Peer) o));
    }

    /**
     * add peer node to the peers queue
     */
    public void add(Peer peer) {
        peerMap.put(peer.getId(), peer);
    }

    public int count() {
        return peerMap.size();
    }

    /**
     * Get the seed peers after starting for each time.
     */
    public void getSeedPeers() {
        List<Peer> peers = peerReqService.doGetSeedPeersRequest();
        LOGGER.info("get peers: {}", peers);
        add(peers);
    }

    /**
     * Get peer instance by peer id.
     */
    public Peer getById(String id) {
        if (StringUtils.isNotEmpty(id)) {
            return peerMap.get(id);
        }
        return null;
    }

    /**
     * Get peer list by peer ids.
     */
    public List<Peer> getByIds(String[] ids) {
        List<Peer> list = Lists.newArrayList();
        if (null != ids) {
            for (String id : ids) {
                Peer peer = getById(id);
                if (peer != null) {
                    list.add(peer);
                }
            }
        }
        return list;
    }

    /**
     * Return all peers from database.
     */
    public Collection<Peer> getPeers() {
        return peerMap.values();
    }

    /**
     * Shuffle limit peers from database.
     */
    public List<Peer> shuffle(int limit) {
        String rnd = RandomStringUtils.randomAlphanumeric(10);
        return getPeers().stream()
                .filter(peer -> null != peer)
                .sorted((o1, o2) -> {
                    String hash1 = Hashing.sha256().hashString(o1.getId() + rnd, Charsets.UTF_8).toString();
                    String hash2 = Hashing.sha256().hashString(o2.getId() + rnd, Charsets.UTF_8).toString();
                    return hash1.compareTo(hash2);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get local peer instance.
     */
    public Peer getSelf() {
        if (null == self) {
            Peer peer = new Peer();
            peer.setIp(config.getClientPublicIp());
            // todo baizhengwen 通过upnp设置端口
            peer.setSocketServerPort(config.getSocketServerPort());
            peer.setHttpServerPort(config.getHttpServerPort());
            peer.setPubKey(config.getPubKey());
            peer.signature(config.getPriKey());

            self = peer;
            add(self);
        }
        return self;
    }

    /**
     * Set value for the local peer and save to database.
     */
    public void setSelf(Peer self) {
        if (null != self) {
            this.self = self;
            add(self);
        }
    }
}


