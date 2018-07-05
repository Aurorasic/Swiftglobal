package com.higgsblock.global.chain.network.socket.message;

import com.higgsblock.global.chain.network.Peer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.LinkedList;

/**
 * send peers to other peers
 *
 * @author zhaoxiaogang
 * @date 2018-05-21
 */
@Data
@NoArgsConstructor
@Slf4j
public class PeersMessage extends BaseMessage {
    private LinkedList<Peer> peers;

    @Override
    public boolean valid() {
        if (CollectionUtils.isEmpty(peers)) {
            return false;
        }
        for (Peer peer : peers) {
            if (peer == null) {
                return false;
            }
            if (!peer.valid()) {
                return false;
            }
        }
        return true;
    }


    public PeersMessage(LinkedList<Peer> peers) {
        this.peers = peers;
    }
}
