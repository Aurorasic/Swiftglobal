package cn.primeledger.cas.global.network.socket.message;

import lombok.Data;


/**
 * Get peers message as the request message for {@link PeersMessage}.
 */

@Data
public class GetPeersMessage extends BaseMessage {

    private static final int MAX_SIZE = 100;

    private int size;

    public GetPeersMessage() {
        this(0);
    }

    public GetPeersMessage(int size) {
        if (size <= 0 || size > MAX_SIZE) {
            this.size = MAX_SIZE;
        } else {
            this.size = size;
        }
    }
}
