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
import org.apache.commons.lang.StringUtils;

/**
 * @author yangyi
 * @deta 2018/5/25
 * @description
 */
@Message(EntityType.RECOMMEND_BLOCK)
@NoArgsConstructor
@Data
@Slf4j
public class RecommendBlock {

    private Block block;
    private String signature;
    private String pubKey;

    public String getHash() {
        HashFunction function = Hashing.sha256();
        String blockHash = block.getHash();
        if (StringUtils.isBlank(blockHash)) {
            blockHash = "empty";
        }
        return function.hashString(blockHash, Charsets.UTF_8).toString();
    }

    public boolean valid() {
        if (block == null) {
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
