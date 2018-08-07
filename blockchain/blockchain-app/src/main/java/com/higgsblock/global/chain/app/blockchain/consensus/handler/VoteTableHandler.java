package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VoteTable;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VotingBlockRequest;
import com.higgsblock.global.chain.app.blockchain.consensus.vote.Vote;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.event.SyncBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.IVoteService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.socket.message.IMessage;
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
    protected boolean valid(IMessage<VoteTable> message) {

        VoteTable data = message.getData();
        //step1:check basic info
        if (null == data
                || !data.valid()) {
            LOGGER.info("valid basic info , false");
            return false;
        }
        return true;
    }

    @Override
    protected void process(IMessage<VoteTable> message) {
        String sourceId = message.getSourceId();
        VoteTable data = message.getData();
        //step2:check witness
        if (!checkVersion1Witness(data)) {
            LOGGER.info("valid witness info , false");
            return;
        }
        long voteHeight = data.getHeight();
        long localHeight = voteService.getHeight();
        //step3: check height
        if (voteHeight < localHeight) {
            LOGGER.info("the height is lower than local,voteHeight={},localHeight={}", voteHeight, localHeight);
            return;
        }
        //step4:if height > my vote height, sync block
        if (voteHeight > localHeight) {
            eventBus.post(new SyncBlockEvent(voteHeight, null, sourceId));
            LOGGER.info("the height is greater than local,sync block,voteHeight={},localHeight={}", voteHeight, localHeight);
            voteService.updateVoteCache(data);
            return;
        }
        //step5: check original block
        if (!checkOriginalBlock(sourceId, data)) {
            LOGGER.info("check original block failed,height={}", voteHeight);
            return;
        }
        //check if this is witness
        if (!witnessService.isWitness(keyPair.getAddress())) {
            messageCenter.dispatchToWitnesses(message.getData());
            LOGGER.info(" dispatch to witnesses");
            return;
        }
        voteService.dealVoteTable(message.getData());
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
                if (!voteService.isExist(voteHeight, k)) {
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
            LOGGER.info("source blocks is not enough,add vote table to cache,height={} , blockHashs={}", voteHeight, blockHashes);
            return false;
        }
        return true;
    }

    private boolean checkVersion1Witness(VoteTable voteTable) {
        Map<Integer, Map<String, Map<String, Vote>>> map = voteTable.getVoteTable();
        if (MapUtils.isEmpty(map)) {
            return false;
        }
        Map<String, Map<String, Vote>> map1 = map.get(1);
        if (MapUtils.isEmpty(map1)) {
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
