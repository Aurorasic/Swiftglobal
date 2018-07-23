package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VoteTable;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VotingBlockRequest;
import com.higgsblock.global.chain.app.blockchain.consensus.vote.Vote;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.IVoteService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
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
    private IVoteService voteService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private IWitnessService witnessService;

    @Override
    protected boolean check(SocketRequest<VoteTable> request) {

        String sourceId = request.getSourceId();
        VoteTable data = request.getData();

        //check if this is witness
        if (!witnessService.isWitness(keyPair.getAddress())) {
            messageCenter.dispatchToWitnesses(data);
            return false;
        }

        //step1:check basic info
        if (null == data
                || !data.valid()) {
            LOGGER.info("valid basic info , false");
            return false;
        }

        //step2:check witness
        if (!checkVersion1Witness(data)) {
            LOGGER.info("valid witness info , false");
            return false;
        }

        long voteHeight = data.getHeight();
        //step3: check height
        if (voteHeight < voteService.getHeight()) {
            return false;
        }

        //step4:if height > my vote height, sync block
        if (voteHeight > voteService.getHeight()) {
            eventBus.post(new ReceiveOrphanBlockEvent(voteHeight, null, sourceId));
            LOGGER.info("the height is greater than local , sync block");
            return false;
        }
        //step5: check original block
        if (!checkOriginalBlock(sourceId, data)) {
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
        Set<String> blockHashes = new HashSet<>();
        map.values().forEach(map1 -> {
            if (MapUtils.isEmpty(map1)) {
                return;
            }
            map1.forEach((k, v) -> {
                if (!voteService.isExistInBlockCache(voteHeight, k)) {
                    blockHashes.add(k);
                }
            });
        });
        if (blockHashes.size() > 0) {
            voteService.updateVoteCache(otherVoteTable);
            if (null != sourceId) {
                messageCenter.unicast(sourceId, new VotingBlockRequest(blockHashes));
            } else {
                messageCenter.dispatchToWitnesses(new VotingBlockRequest(blockHashes));
            }
            LOGGER.info("source blocks is not enough,add vote table to cache");
            return false;
        }
        return true;
    }

    private boolean checkVersion1Witness(VoteTable voteTable) {
        Map<Integer, Map<String, Map<String, Vote>>> map = voteTable.getVoteTable();
        if (MapUtils.isNotEmpty(map)) {
            return false;
        }
        Map<String, Map<String, Vote>> map1 = map.get(1);
        if (MapUtils.isNotEmpty(map1)) {
            return false;
        }
        for (String witnessPubKey : map1.keySet()) {
            if (!witnessService.isWitness(ECKey.pubKey2Base58Address(witnessPubKey))) {
                return false;
            }
        }
        return true;

    }
}
