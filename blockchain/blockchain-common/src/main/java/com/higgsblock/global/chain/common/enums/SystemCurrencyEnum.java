package com.higgsblock.global.chain.common.enums;


import lombok.Getter;

/**
 * @author yuguojia
 * @date 2018/03/08
 **/
@Getter
public enum SystemCurrencyEnum {
    /**
     * CAS
     */
    CAS("cas"),

    /**
     * the stack currency who could be selected to mine/produce block
     */
    MINER("miner"),
    /**
     * add guarder_miner coin
     */
    GUARDER("guarder");

    private String currency;

    SystemCurrencyEnum(String currency) {
        this.currency = currency;
    }
}