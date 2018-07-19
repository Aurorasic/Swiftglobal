package com.higgsblock.global.chain.app.sync.message;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date 7/18/2018
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Message(MessageType.BLOCK_RESPONSE)
public class BlockResponse {

    private int version = 0;

    private Block block;
}
