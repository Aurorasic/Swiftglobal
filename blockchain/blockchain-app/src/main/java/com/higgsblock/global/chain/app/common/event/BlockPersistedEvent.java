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
public class BlockPersistedEvent extends BaseSerializer {
    /**
     * the height of the persisted block
     */
    private long height;

    /**
     * the block-hash of the persisted block
     */
    private String blockHash;

    /**
     * the height of the highest best block
     */
//    private long highestBestHeight;

    /**
     * the best block hash on highest best block.
     */
//    private String highestBestBlockHash;

    /**
     * if it is true, that is after persisting this block the "highestBestBlockHash" is newest best block
     * it is false, that is after persisting this block the "highestBestBlockHash" is old, on newest best block to be confirmed
     */
    private boolean isConfirmedNewBestBlock;
}