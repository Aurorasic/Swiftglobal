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
import org.apache.logging.log4j.util.Strings;

import java.util.List;

/**
 * @author yangyi
 * @deta 2018/5/25
 * @description
 */
@Message(EntityType.CANDIDATE_BLOCK_HASH)
@NoArgsConstructor
@Data
@Slf4j
public class CandidateBlockHashs {
    private List<String> blockHashs;
    private long height;
    private String address;
    private String signature;
    private String pubKey;

    public String getHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(function.hashLong(height))
                .append(function.hashString(null == address ? Strings.EMPTY : address, Charsets.UTF_8));
        if (CollectionUtils.isNotEmpty(blockHashs)) {
            blockHashs.forEach(blockHash -> builder.append(blockHash));
        }
        return function.hashString(builder.toString(), Charsets.UTF_8).toString();
    }

    public boolean valid() {
        if (blockHashs == null) {
            return false;
        }
        if (height < 1) {
            return false;
        }
        if (StringUtils.isBlank(pubKey) || StringUtils.isBlank(signature) || StringUtils.isBlank(address)) {
            return false;
        }
        return StringUtils.equals(ECKey.pubKey2Base58Address(pubKey), address) && ECKey.verifySign(getHash(), signature, pubKey);
    }


}
