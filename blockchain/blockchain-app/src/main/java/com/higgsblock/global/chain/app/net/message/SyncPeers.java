package com.higgsblock.global.chain.app.net.message;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.net.peer.Peer;
import lombok.Data;

import java.util.LinkedList;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
@Message(MessageType.SYNC_PEERS)
@Data
public class SyncPeers {

    private LinkedList<Peer> peers;

}
