package com.higgsblock.global.chain.app.consensus.vote;

import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.constants.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * @author yuanjiantao
 * @date 7/2/2018
 */
@Message(EntityType.SOURCE_BLOCK_REQ)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class SourceBlockReq {

    private Set<String> blockHashs;
}
