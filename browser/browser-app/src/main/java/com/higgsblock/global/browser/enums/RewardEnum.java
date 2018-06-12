package com.higgsblock.global.browser.enums;

import lombok.Getter;

/**
 * @author Su Jiulong
 * @date 2018-05-24
 */
@Getter
public enum RewardEnum {
    /**
     * Reward type
     */
    WITNESS_REWARD(1, "witness_reward"),
    MINER_REWARD(0, "miner_reward");

    private String type;
    private int code;

    RewardEnum(int code, String type) {
        this.code = code;
        this.type = type;
    }
}
