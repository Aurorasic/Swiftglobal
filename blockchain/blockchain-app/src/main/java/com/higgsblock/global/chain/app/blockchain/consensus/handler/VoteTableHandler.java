package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.higgsblock.global.chain.app.blockchain.consensus.message.VoteTable;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.VoteProcessor;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 6/29/2018
 */
@Component
@Slf4j
public class VoteTableHandler extends BaseMessageHandler<VoteTable> {

    @Autowired
    private VoteProcessor voteProcessor;

    @Override
    protected void process(SocketRequest<VoteTable> request) {

        VoteTable data = request.getData();
        String sourceId = request.getSourceId();
        LOGGER.info("Received VoteTable from {} with data {}", sourceId, data.toJson());
        if (data == null || !data.valid()) {
            LOGGER.info("the voteTable is not validate from {}", sourceId);
            return;
        }
        long voteHeight = data.getVoteHeight();
        if (voteHeight == -1) {
            LOGGER.info("the voteHeight is wrong");
            return;
        }
        LOGGER.info("add voteTable with voteHeight {}", voteHeight);
        voteProcessor.dealVoteTable(sourceId, voteHeight, data);
    }
}
