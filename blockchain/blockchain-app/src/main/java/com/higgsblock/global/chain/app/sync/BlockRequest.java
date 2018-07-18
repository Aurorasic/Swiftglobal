package com.higgsblock.global.chain.app.sync;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Message(MessageType.BLOCK_REQUEST)
public class BlockRequest extends BaseBizEntity {

    private long height;

    private String hash;

    public BlockRequest(long height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof BlockRequest) {
            BlockRequest blockRequest = (BlockRequest) o;
            return this.height == blockRequest.height && StringUtils.equals(this.hash, blockRequest.hash);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (height + (null == hash ? 0L : hash.hashCode()));
    }
}
