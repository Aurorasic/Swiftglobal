package com.higgsblock.global.chain.app.net.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Give a connection a level which is used for connection pool managing. That is to say, a connection of a lower level
 * will always be kept out of the pool when cache is full.
 *
 * @author chenjiawei
 * @date 2018-05-04
 */
@Getter
@AllArgsConstructor
public enum ConnectionLevelEnum {
    /**
     * a connection between two witnesses will have a highest level.
     */
    L1((byte) 1),

    /**
     * a connection between a miner and a witness will have a middle level.
     */
    L2((byte) 2),

    /**
     * a connection between two miners, or a peer and any node will have a lowest level.
     */
    L3((byte) 3);

    /**
     * the more smaller the value is, the more higher the level is.
     */
    private byte level;
}
