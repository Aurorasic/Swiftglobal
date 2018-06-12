package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.constants.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yuanjiantao
 * @date 5/25/2018
 */
@Message(EntityType.SOURCE_BLOCK)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class SourceBlock {

    private Block block;
}
