package com.higgsblock.global.chain.app.blockchain.consensus.vote;

import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.VoteProcessor;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 6/29/2018
 */
@Component
@Slf4j
public class VoteTableNotifyHandler extends BaseEntityHandler<VoteTableNotify> {

    @Autowired
    private VoteProcessor voteProcessor;

    @Override
    protected void process(SocketRequest<VoteTableNotify> request) {

        VoteTableNotify data = request.getData();
        String sourceId = request.getSourceId();
        LOGGER.info("Received VoteTable from {} with data {}", sourceId, JSON.toJSONString(data));
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
