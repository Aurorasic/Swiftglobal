package com.higgsblock.global.chain.app.blockchain;

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
@Message(MessageType.SOURCE_BLOCK_RESPONSE)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class SourceBlockResponse {

    private Block block;
}
