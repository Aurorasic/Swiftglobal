package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.constants.EntityType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author yangyi
 * @deta 2018/5/25
 * @description
 */
@Message(EntityType.QUERY_CANDIDATE_BLOCK)
@NoArgsConstructor
@Data
@Slf4j
public class QueryCandidateBlocks {
    private List<String> blockHashs;
}
