package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.constants.EntityType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yangyi
 * @deta 2018/5/25
 * @description
 */
@Message(EntityType.QUERY_CANDIDATE_BLOCK_HASH)
@NoArgsConstructor
@Data
@Slf4j
public class QueryCandidateBlockHashs {
    private long height;
}
