package com.higgsblock.global.chain.app.sync;

import com.higgsblock.global.chain.app.common.constants.EntityType;
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
@Message(EntityType.GET_BLOCK_REQ)
public class BlockReq extends BaseBizEntity {

    private long height;

    private String hash;

    public BlockReq(long height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof BlockReq) {
            BlockReq blockReq = (BlockReq) o;
            return this.height == blockReq.height && StringUtils.equals(this.hash, blockReq.hash);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (height + (null == hash ? 0L : hash.hashCode()));
    }
}
