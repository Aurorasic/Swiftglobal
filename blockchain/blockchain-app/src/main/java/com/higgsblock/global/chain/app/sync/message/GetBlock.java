package com.higgsblock.global.chain.app.sync.message;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
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
@Message(MessageType.GET_BLOCK)
public class GetBlock extends BaseSerializer {

    private int version = 0;

    private long height;

    private String hash;

    public GetBlock(long height, String hash) {
        this.height = height;
        this.hash = hash;
    }

    public GetBlock(long height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof GetBlock) {
            GetBlock getBlock = (GetBlock) o;
            return this.height == getBlock.height && StringUtils.equals(this.hash, getBlock.hash);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (height + (null == hash ? 0L : hash.hashCode()));
    }

    public boolean valid() {
        return height > 1;
    }
}
