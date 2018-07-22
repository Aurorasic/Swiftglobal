package com.higgsblock.global.chain.app.blockchain.consensus.message;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yuanjiantao
 * @date 5/25/2018
 */
@Message(MessageType.VOTING_BLOCK_RESPONSE)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class VotingBlockResponse {
    public VotingBlockResponse(Block block) {
        this.block = block;
    }

    private int version = 0;

    private Block block;

    public boolean valid() {
        return null != block && block.valid() && version >= 0;
    }
}
