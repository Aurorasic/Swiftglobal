package cn.primeledger.cas.global.api.service;

import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.utils.Rnd;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PeerRespService {
    private static int DEFAULT_RETURN_COUNT = 30;

    @Autowired
    private ConcurrentMap<String, Peer> peerMap;


    public boolean peerRegister(Peer peer) {
        if (peer == null ) {
            LOGGER.info("Peer is null");
            return false;
        }

        if (!peer.validParams()) {
            LOGGER.info("Invalid peer parameters");
            return false;
        }

        if (!peer.validSignature()) {
            LOGGER.info("Invalid signature");
            return false;
        }

        String address = ECKey.pubKey2Base58Address(peer.getPubKey());
        peerMap.put(address, peer);

        return true;
    }

    public Peer getPeer(String address) {
        return peerMap.get(address);
    }

    public List<Peer> getPeers(List<String> addressList) {
        List<Peer> peers = new LinkedList<>();
        addressList.stream().forEach(address -> {
            Peer peer = peerMap.get(address);
            if (peer != null) {
                peers.add(peer);
            }
        });
        return peers;
    }

    /**
     * If the DB has the peers lower the {@link #DEFAULT_RETURN_COUNT},
     * will return all the value except self. Otherwise, it will return
     * {@link #DEFAULT_RETURN_COUNT} peers randomly.
     */
    public List<Peer> getSeedPeerList() {
        final Set<String> mapKeys = peerMap.keySet();
        final Set<String> resKeys = new HashSet<>();

        if (mapKeys != null) {
            if (mapKeys.size() <= DEFAULT_RETURN_COUNT) {
                resKeys.addAll(mapKeys);
            } else {
                List<String> keyList = mapKeys.stream()
                        .collect(Collectors.toList());

                while (resKeys.size() < DEFAULT_RETURN_COUNT) {
                    resKeys.add(keyList.get(getRandomMode()));
                }
            }
        }

        List<Peer> peers = Lists.newLinkedList();
        resKeys.stream().forEach(key -> {
            peers.add(peerMap.get(key));
        });

        return peers;
    }

    private int getRandomMode() {
        return (int) Math.abs(Rnd.rndLong() % DEFAULT_RETURN_COUNT);
    }
}
