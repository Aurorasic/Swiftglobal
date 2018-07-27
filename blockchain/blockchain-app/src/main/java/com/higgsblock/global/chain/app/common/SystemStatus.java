package com.higgsblock.global.chain.app.common;

import java.io.Serializable;

/**
 * @author yuguojia
 * @date 2018/03/29
 **/
public enum SystemStatus implements Serializable {

    INI(1, "init status"),
    LOADING(2, "load all block data on local"),
    SYNC_BLOCKS(3, "sync blocks with other peers"),
    SYNC_FINISHED(4, "sync blocks finished"),
    RUNNING(5, "running status");

    private int state;
    private String desc;

    SystemStatus(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public boolean equals(SystemStatus otherState) {
        if (this.state == otherState.state) {
            return true;
        }
        return false;
    }
}