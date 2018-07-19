package com.higgsblock.global.chain.app.blockchain.consensus.message;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yangyi
 * @deta 2018/7/19
 * @description
 */
@Message(MessageType.ORIGIN_BLOCK)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class OriginBlock extends BaseSerializer {
    private Block block;
}
