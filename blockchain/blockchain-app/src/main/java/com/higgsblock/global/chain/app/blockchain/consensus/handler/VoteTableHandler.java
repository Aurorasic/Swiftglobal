package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VoteTable;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VotingBlockRequest;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.VoteService;
import com.higgsblock.global.chain.app.blockchain.consensus.vote.Vote;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.impl.WitnessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author yuanjiantao
 * @date 6/29/2018
 */
@Component
@Slf4j
public class VoteTableHandler extends BaseMessageHandler<VoteTable> {

    @Autowired
    private VoteService voteService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private EventBus eventBus;

    @Override
    protected boolean check(SocketRequest<VoteTable> request) {
        String sourceId = request.getSourceId();
        VoteTable data = request.getData();

        //step1:check basic info
        if (null == data
                || !data.valid(WitnessService.WITNESS_ADDRESS_LIST)) {
            return false;
        }

        long voteHeight = data.getHeight();
        //step2: check height
        if (voteHeight < voteService.getHeight()) {
            return false;
        }

        //step3:if height > my vote height, sync block
        if (voteHeight > voteService.getHeight()) {
            eventBus.post(new ReceiveOrphanBlockEvent(voteHeight, null, sourceId));
            LOGGER.info("the height is greater than local , sync block");
            return false;
        }
        //step4: check original block
        if (checkOriginalBlock(sourceId, data)) {
            return false;
        }
        return true;
    }

    @Override
    protected void process(SocketRequest<VoteTable> request) {
        voteService.dealVoteTable(request.getData());
    }

    private boolean checkOriginalBlock(String sourceId, VoteTable otherVoteTable) {
        long voteHeight = otherVoteTable.getHeight();
        Map<String, Map<String, Vote>> map = otherVoteTable.getVoteMapOfPubKeyByVersion(1);
        if (MapUtils.isEmpty(map)) {
            return false;
        }
        Set<String> blockHashs = new HashSet<>();
        map.values().forEach(map1 -> {
            if (MapUtils.isEmpty(map1)) {
                return;
            }
            map1.forEach((k, v) -> {
                if (!voteService.isExistInBlockCache(voteHeight, k)) {
                    blockHashs.add(k);
                }
            });
        });
        if (blockHashs.size() > 0) {
            voteService.updateVoteCache(otherVoteTable);
            if (null != sourceId) {
                messageCenter.unicast(sourceId, new VotingBlockRequest(blockHashs));
            } else {
                messageCenter.dispatchToWitnesses(new VotingBlockRequest(blockHashs));
            }
            LOGGER.info("source blocks is not enough,add vote table to cache");
            return false;
        }
        return true;
    }
}
