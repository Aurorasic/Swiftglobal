package com.higgsblock.global.chain.app.common;

import lombok.Getter;

/**
 * @author yuguojia
 * @date 2018/04/03
 **/
@Getter
public enum SystemStepEnum {
    LOADED_ALL_DATA(1, "加载完所有区块数据"),

    SYNCED_BLOCKS(2, "区块数据同步完成");

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