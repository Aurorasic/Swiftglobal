package com.higgsblock.global.chain.app.common;

import lombok.Getter;

/**
 * @author yuguojia
 * @date 2018/04/03
 **/
@Getter
public enum SystemStepEnum {
    LOADED_ALL_DATA(1, "loaded all block data"),

    CHECK_DATA(2, "start check data"),

    SYNCED_BLOCKS(3, "switched all blocks with neighbor peers"),

    START_FINISHED(4, "started all resources");

    private int type;
    private String desc;

    SystemStepEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public boolean equals(SystemStepEnum otherEvent) {
        if (otherEvent != null && this.type == otherEvent.type) {
            return true;
        }
        return false;
    }

}