package cn.primeledger.cas.global.blockchain.transaction;

import lombok.Getter;

/**
 * @author yuguojia
 * @date 2018/03/08
 **/
@Getter
public enum SystemCurrencyEnum {

    COMMUNITY_MANAGER("communityManager"),
    CAS("cas"),
    MINER("miner"),
    ISSUE_TOKEN("issue_tokens");

    private String currency;

    SystemCurrencyEnum(String currency) {
        this.currency = currency;
    }
}