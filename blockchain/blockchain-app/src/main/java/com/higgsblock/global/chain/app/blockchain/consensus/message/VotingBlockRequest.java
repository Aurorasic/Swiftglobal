package com.higgsblock.global.chain.app.blockchain.consensus.message;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * @author yuanjiantao
 * @date 7/2/2018
 */
@Message(MessageType.VOTING_BLOCK_REQUEST)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class VotingBlockRequest extends BaseSerializer {

    private Set<String> blockHashs;
}
