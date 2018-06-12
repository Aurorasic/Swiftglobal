package com.higgsblock.global.chain.app.blockchain;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.network.Peer;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuweizhen
 * @date 2018-05-21
 */
@Data
@NoArgsConstructor
public class WitnessEntity {
    private String ip;
    private int socketPort;
    private int httpPort;

    private String pubKey;
    private String address;

    private int version;
    private String signature;

    private long heightStart;
    private long heightEnd;
    private long addTimeStamp;

    public static List<Peer> witnessEntity2Peer(List<WitnessEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>(0);
        }

        List<Peer> peers = Lists.newArrayList();
        list.forEach(witnessEntity -> {
            peers.add(witnessEntity2Peer(witnessEntity));
        });

        return peers;
    }

    public static Peer witnessEntity2Peer(WitnessEntity witnessEntity) {
        if (witnessEntity == null) {
            return null;
        }

        Peer peer = new Peer();
        peer.setVersion(witnessEntity.getVersion());
        peer.setIp(witnessEntity.getIp());
        peer.setSocketServerPort(witnessEntity.getSocketPort());
        peer.setHttpServerPort(witnessEntity.getHttpPort());
        peer.setPubKey(witnessEntity.getPubKey());
        peer.setSignature(witnessEntity.getSignature());

        return peer;
    }
}
