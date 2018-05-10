package com.higgsblock.global.chain.network.socket.message;

import com.higgsblock.global.chain.network.Peer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hello ack message as the response message for {@link HelloMessage}. It will be sent
 * by the server when received the hello message from client.
 *
 * @author yuanjiantao
 * @date Created in 2/28/2018
 */

@Data
@NoArgsConstructor
public class HelloAckMessage extends BaseMessage {
    private Peer peer;

    @Override
    public boolean valid(){
        if (peer == null){
            return false;
        }
        return peer.valid();
    }
    public HelloAckMessage(Peer peer) {
        this.peer = peer;
    }
}
