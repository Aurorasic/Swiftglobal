package com.higgsblock.global.chain.app.consensus.vote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yuanjiantao
 * @date 6/29/2018
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class Vote {

    private int version;

    private long height;

    private String witnessPubKey;

    private String blockHash;

    private String proofPubKey;

    private String proofVersion;

    private String signature;
}
