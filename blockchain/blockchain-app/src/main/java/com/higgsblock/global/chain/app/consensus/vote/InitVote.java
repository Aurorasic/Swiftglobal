package com.higgsblock.global.chain.app.consensus.vote;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.constants.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yuanjiantao
 * @date 6/29/2018
 */
@Message(EntityType.INITVOTE)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class InitVote {

    private Vote vote;

    private Block block;
}
