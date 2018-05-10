package com.higgsblock.global.chain.network.socket.message;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base message is the parent class for all p2p messages.
 *
 * @author yuanjiantao
 * @date 2/26/2018
 */
@Data
@NoArgsConstructor
public abstract class BaseMessage extends BaseSerializer {

    public boolean valid() {
        return true;
    }

}
