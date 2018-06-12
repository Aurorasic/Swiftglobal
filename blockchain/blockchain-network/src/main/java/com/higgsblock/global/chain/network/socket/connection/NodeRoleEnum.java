package com.higgsblock.global.chain.network.socket.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Give a node a role which is used for calculating connection level. The role is temporary, and may change once a
 * block is linked into the main chain.
 *
 * @author chenjiawei
 * @date 2018-05-04
 */
@AllArgsConstructor
@Getter
public enum NodeRoleEnum {
    /**
     * a node of this role is an ordinary peer, and it can not do mining current time.
     */
    PEER(1),

    /**
     * a node of this role does mining current time.
     */
    MINER(2),

    /**
     * a node of this role takes part in selecting main block packaged by miners.
     */
    WITNESS(3);

    /**
     * connection level depend on weight of the two nodes in a connection.
     */
    private int weight;
}
