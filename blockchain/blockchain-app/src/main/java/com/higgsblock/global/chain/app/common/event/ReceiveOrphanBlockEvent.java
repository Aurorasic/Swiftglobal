package com.higgsblock.global.chain.app.common.event;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuguojia
 * @date 2018/04/04
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveOrphanBlockEvent extends BaseSerializer {
    /**
     * the height of the orphan block
     */
    private long height;

    /**
     * the block-hash of the orphan block
     */
    private String blockHash;

    private String sourceId;
}