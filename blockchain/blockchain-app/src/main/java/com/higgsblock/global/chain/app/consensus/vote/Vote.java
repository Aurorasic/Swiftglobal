package com.higgsblock.global.chain.app.consensus.vote;

import com.higgsblock.global.chain.app.entity.BaseBizEntity;
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
public class Vote extends BaseBizEntity {

    private int voteVersion;

    private long height;

    private String witnessPubKey;

    private String blockHash;

    private String proofPubKey;

    private String proofBlockHash;

    private String preBlockHash;

    private int proofVersion;

    private String signature;
}
