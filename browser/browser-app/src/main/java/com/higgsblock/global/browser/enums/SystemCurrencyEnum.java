package com.higgsblock.global.browser.enums;

import lombok.Getter;

/**
 * @author yuguojia
 * @date 2018/03/08
 **/
@Getter
public enum SystemCurrencyEnum {

    /**
     * currency
     */
    COMMUNITY_MANAGER("communityManager"),
    CAS("cas"),
    MINER("miner"),
    C_MINER("cMiner"),
    ISSUE_TOKEN("issue_tokens");

    private String currency;

    SystemCurrencyEnum(String currency) {
        this.currency = currency;
    }
}