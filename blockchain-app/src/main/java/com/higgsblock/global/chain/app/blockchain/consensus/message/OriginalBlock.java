package com.higgsblock.global.chain.app.blockchain.consensus.message;

import com.alibaba.fastjson.annotation.JSONType;
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
@Message(MessageType.ORIGINAL_BLOCK)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
@JSONType(includes = {"version", "block"})
public class OriginalBlock extends BaseSerializer {

    private int version = 0;

    private Block block;

    public OriginalBlock(Block block) {
        this.block = block;
    }

    public boolean valid() {
        return block.valid() && version >= 0;
    }
}
