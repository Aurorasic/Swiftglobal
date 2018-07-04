package com.higgsblock.global.chain.app.consensus.vote;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.HashBasedTable;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * @author yuanjiantao
 * @date 6/29/2018
 */
@Component("allVoteHandler")
@Slf4j
public class VoteTableHandler extends BaseEntityHandler<VoteTable> {

    @Autowired
    private WitnessService witnessService;

    @Autowired
    private MessageCenter messageCenter;

    @Override
    protected void process(SocketRequest<VoteTable> request) {


        VoteTable data = request.getData();
        String sourceId = request.getSourceId();
        LOGGER.info("Received VoteTable from {} with data {}", sourceId, JSON.toJSONString(data));
        HashBasedTable<Integer, String, Map<String, Vote>> voteTable = data.getVoteTable();
        if (voteTable == null || voteTable.size() == 0) {
            LOGGER.info("the voteTable is null from {}", sourceId);
            return;
        }
        Map<String, Map<String, Vote>> row = voteTable.row(Integer.valueOf(1));
        if (row == null || row.values().size() == 0) {
            LOGGER.info("the voteTable hasn't vote which voteVersion is one from {}", sourceId);
            return;
        }
        Iterator<Map<String, Vote>> iterator = row.values().iterator();
        Map<String, Vote> voteMap = null;
        long voteHeight = -1;
        while (iterator.hasNext()) {
            voteMap = iterator.next();
            if (voteMap == null || voteMap.size() != 1) {
                continue;
            }
            Vote vote = voteMap.get(0);
            if (vote != null) {
                voteHeight = vote.getHeight();
            }
            if (voteHeight != -1) {
                break;
            }
        }
        if (voteHeight == -1) {
            LOGGER.info("the voteHeight is wrong");
            return;
        }
        LOGGER.info("add voteTable with voteHeight {} ,voteTable {}", voteHeight, voteTable);

        witnessService.dealVoteTable(sourceId, voteHeight, voteTable);




    }
}
