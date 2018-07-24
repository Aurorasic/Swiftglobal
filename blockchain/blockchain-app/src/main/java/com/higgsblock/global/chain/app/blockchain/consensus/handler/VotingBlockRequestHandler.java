package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VotingBlockRequest;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VotingBlockResponse;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import com.higgsblock.global.chain.app.service.IVoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 7/2/2018
 */
@Component
@Slf4j
public class VotingBlockRequestHandler extends BaseMessageHandler<VotingBlockRequest> {

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private IVoteService voteService;

    @Override
    protected boolean check(IMessage<VotingBlockRequest> message) {
        VotingBlockRequest data = message.getData();
        if (null == data || CollectionUtils.isEmpty(data.getBlockHashes())) {
            return false;
        }
        return true;
    }

    @Override
    protected void process(IMessage<VotingBlockRequest> message) {
        String sourceId = message.getSourceId();
        VotingBlockRequest data = message.getData();
        LOGGER.info("received originalBlockRequest from {} with data {}", sourceId, data);
        data.getBlockHashes().forEach(hash -> {
            Block block = voteService.getVotingBlock(hash);
            if (null != block) {
                messageCenter.unicast(sourceId, new VotingBlockResponse(block));
            }
        });
    }
}
