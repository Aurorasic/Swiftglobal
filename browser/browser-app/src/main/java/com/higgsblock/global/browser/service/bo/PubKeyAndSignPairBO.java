package com.higgsblock.global.browser.service.bo;

import lombok.Data;

/**
 * @author Su Jiulong
 * @date 2018-05-25
 */
@Data
public class PubKeyAndSignPairBO {
    /**
     * public key
     */
    private String pubKey;
    /**
     * miner's signature
     */
    private String signature;
}
