package com.higgsblock.global.chain.app.common;

import lombok.Getter;

/**
 * @author yuguojia
 * @date 2018/04/03
 **/
@Getter
public enum SystemStepEnum {
    LOADED_ALL_DATA(1, "loaded all block data"),

    SYNCED_BLOCKS(2, "switched all blocks with neighbor peers"),

    START_CHECK_DATA(3, "start check data");

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