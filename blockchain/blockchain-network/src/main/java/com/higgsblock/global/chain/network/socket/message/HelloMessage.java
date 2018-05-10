package com.higgsblock.global.chain.network.socket.message;

import com.higgsblock.global.chain.network.Peer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hello message as the request message for {@link HelloAckMessage}. It will be sent
 * by the client when the connection with the peer is active.
 *
 * @author yuanjiantao
 * @date Created in 2/27/2018
 */
@Data
@NoArgsConstructor
public class HelloMessage extends BaseMessage {

    private Peer peer;

    @Override
    public boolean valid(){
        if (peer == null){
            return false;
        }
        return  peer.valid();
    }

    public HelloMessage(Peer peer) {
        this.peer = peer;
    }
}
