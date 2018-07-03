package com.higgsblock.global.chain.app.blockchain;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.constants.EntityType;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author yangyi
 * @deta 2018/5/25
 * @description
 */
@Message(EntityType.CANDIDATE_BLOCK)
@NoArgsConstructor
@Data
@Slf4j
public class CandidateBlock {
    private List<Block> blocks;
    private long height;
    private String signature;
    private String pubKey;

    public String getHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(function.hashLong(height));
        if (CollectionUtils.isNotEmpty(blocks)) {
            blocks.forEach(block -> builder.append(block.getHash()));
        }
        return function.hashString(builder.toString(), Charsets.UTF_8).toString();
    }

    public boolean valid() {
        if (blocks == null) {
            return false;
        }
        if (height < 1) {
            return false;
        }
        if (StringUtils.isBlank(pubKey) || StringUtils.isBlank(signature)) {
            return false;
        }
        if (!ECKey.verifySign(getHash(), signature, pubKey)) {
            return false;
        }
        return true;
    }
}
