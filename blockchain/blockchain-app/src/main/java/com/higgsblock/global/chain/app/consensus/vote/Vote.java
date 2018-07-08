package com.higgsblock.global.chain.app.consensus.vote;

import com.alibaba.fastjson.annotation.JSONField;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

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

    private String signature;

    private String proofPubKey;

    private String proofBlockHash;

    private String preBlockHash;

    private int proofVersion;

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Vote) {
            Vote anotherVote = (Vote) o;
            return this.height == anotherVote.height
                    && this.voteVersion == anotherVote.voteVersion
                    && StringUtils.equals(this.witnessPubKey, anotherVote.witnessPubKey)
                    && StringUtils.equals(this.blockHash, anotherVote.blockHash)
                    && StringUtils.equals(this.proofPubKey, anotherVote.proofPubKey)
                    && StringUtils.equals(this.proofBlockHash, anotherVote.proofBlockHash)
                    && StringUtils.equals(this.preBlockHash, anotherVote.preBlockHash)
                    && StringUtils.equals(this.signature, anotherVote.signature)
                    && this.proofVersion == anotherVote.proofVersion;
        }
        return false;
    }

    @Override
    public boolean valid() {

        if (version < 0) {
            return false;
        }
        if (height < 0) {
            return false;
        }
        if (voteVersion < 0 || proofVersion < 0 || proofVersion > voteVersion) {
            return false;
        }
        if (StringUtils.isEmpty(witnessPubKey)) {
            return false;
        }
        if (StringUtils.isEmpty(blockHash)) {
            return false;
        }
        if (StringUtils.isEmpty(signature)) {
            return false;
        }
        if (!BlockService.validSign(height, blockHash, voteVersion, signature, witnessPubKey)) {
            return false;
        }
        if (voteVersion > 1) {
            if (StringUtils.isEmpty(proofPubKey)) {
                return false;
            }
            if (StringUtils.isEmpty(proofBlockHash)) {
                return false;
            }
            if (StringUtils.isEmpty(preBlockHash)) {
                return false;
            }
            //follower's vote
            if (!isLeaderVote()) {
                boolean isValid = false;
                if (blockHash.compareTo(proofBlockHash) == 0 && proofBlockHash.compareTo(preBlockHash) >= 0) {
                    isValid = true;
                } else if (blockHash.compareTo(preBlockHash) == 0 && preBlockHash.compareTo(proofBlockHash) >= 0) {
                    isValid = true;
                }
                if (!isValid) {
                    return false;
                }
            }
            //leader's vote
            else if (blockHash.compareTo(proofBlockHash) != 0 || blockHash.compareTo(preBlockHash) <= 0) {
                return false;
            }
        }
        return true;
    }

    @JSONField(serialize = false)
    public boolean isLeaderVote() {
        return proofVersion < voteVersion;
    }
}
