package com.higgsblock.global.chain.app.blockchain.consensus.vote;

import com.higgsblock.global.chain.app.common.constants.EntityType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author yuanjiantao
 * @date 6/28/2018
 */
@Message(EntityType.VOTES_NOTIFY)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class VoteTableNotify extends BaseBizEntity {

    private Map<Integer, Map<String, Map<String, Vote>>> voteTable;

}
