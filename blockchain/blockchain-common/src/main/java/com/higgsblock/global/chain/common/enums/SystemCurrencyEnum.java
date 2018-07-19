package com.higgsblock.global.chain.common.enums;


import lombok.Getter;

/**
 * @author yuguojia
 * @date 2018/03/08
 **/
@Getter
public enum SystemCurrencyEnum {

    /**
     * the stack currency who has some community authorities
     */
    COMMUNITY("community"),

    /**
     * CAS
     */
    CAS("cas"),

    /**
     * the stack currency who could be selected to mine/produce block
     */
    MINER("miner"),
    /**
     * add guarder coin
     */
    GUARDER("guarder"),
    /**
     * the stack currency who could issue token
     */
    ISSUE_TOKEN("issue_tokens");

    private String currency;

    SystemCurrencyEnum(String currency) {
        this.currency = currency;
    }
}