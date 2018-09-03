package com.higgsblock.global.chain.app.sync.message;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuanjiantao
 * @date 5/25/2018
 */
@Message(MessageType.BLOCK_RESPONSE)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class BlockResponse {
    private int version = 0;
    private long height;
    private List<Block> blocks;

    public BlockResponse(long height, Block block) {
        this.height = height;
        List<Block> blocks = new ArrayList<>();
        blocks.add(block);
        this.blocks = blocks;
    }

    public BlockResponse(long height, List<Block> blocks) {
        this.height = height;
        this.blocks = blocks;
    }

    public boolean valid() {
        return CollectionUtils.isNotEmpty(blocks) && height >= 1 && version >= 0
                && blocks.stream().allMatch(block -> block.getHeight() == height && block.valid());
    }
}
