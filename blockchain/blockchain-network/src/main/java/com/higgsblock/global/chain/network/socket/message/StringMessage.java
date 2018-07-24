package com.higgsblock.global.chain.network.socket.message;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * Text type message
 *
 * @author yuanjiantao
 * @date Created in 3/1/2018
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StringMessage extends BaseSerializer {

    private String sourceId;
    private String content;

    public boolean valid() {
        if (StringUtils.isEmpty(content)) {
            return false;
        }
        return true;
    }

    public String getHash() {
        return Hashing.goodFastHash(128).newHasher()
                .putString(null == content ? StringUtils.EMPTY : content, Charsets.UTF_8)
                .hash()
                .toString();
    }
}
