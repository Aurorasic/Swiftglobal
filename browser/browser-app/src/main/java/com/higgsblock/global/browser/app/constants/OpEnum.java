package com.higgsblock.global.browser.app.constants;

import lombok.Getter;

/**
 * @author Su Jiulong
 * @date 2018-05-28
 */
@Getter
public enum OpEnum {
    /**
     * The node's all transaction
     */
    ALL("all"),
    /**
     * The node's input transaction
     */
    IN("in"),
    /**
     * The node's output transaction
     */
    OUT("out");

    private String type;

    OpEnum(String type) {
        this.type = type;
    }
}
