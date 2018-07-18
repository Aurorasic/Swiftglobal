package com.higgsblock.global.chain.app.consensus.vote;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.HashBasedTable;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.consensus.sign.service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author yuanjiantao
 * @date 6/29/2018
 */
@Component("voteTableHandler")
@Slf4j
public class VoteTableHandler extends BaseEntityHandler<VoteTable> {

    @Autowired
    private VoteService voteService;

    @Override
    protected void process(SocketRequest<VoteTable> request) {

        VoteTable data = request.getData();
        String sourceId = request.getSourceId();
        LOGGER.info("Received VoteTable from {} with data {}", sourceId, JSON.toJSONString(data));
        Map<Integer, Map<String, Map<String, Vote>>> voteTableMap = data.getVoteTable();
        if (voteTableMap == null || voteTableMap.size() == 0) {
            LOGGER.info("the voteTable is null from {}", sourceId);
            return;
        }
        Set<Map.Entry<Integer, Map<String, Map<String, Vote>>>> entries = voteTableMap.entrySet();
        HashBasedTable<Integer, String, Map<String, Vote>> voteTable = HashBasedTable.create();
        entries.forEach(entry -> {
            Integer version = entry.getKey();
            Map<String, Map<String, Vote>> value = entry.getValue();
            if (value != null) {
                value.entrySet().forEach(vote -> {
                    String votePubKye = vote.getKey();
                    Map<String, Vote> voteMap = vote.getValue();
                    voteTable.put(version, votePubKye, voteMap);
                });
            }
        });
        Map<String, Map<String, Vote>> row = voteTable.row(Integer.valueOf(1));
        if (row == null || row.values().size() == 0) {
            LOGGER.info("the voteTable hasn't vote which voteVersion is one from {}", sourceId);
            return;
        }
        long voteHeight = getVoteHeight(row);
        if (voteHeight == -1) {
            LOGGER.info("the voteHeight is wrong");
            return;
        }
        LOGGER.info("add voteTable with voteHeight {} ,voteTable size: {}", voteHeight, voteTable.size());

        voteService.dealVoteMap(sourceId, voteHeight, voteTableMap);
    }

    private long getVoteHeight(Map<String, Map<String, Vote>> row) {
        Iterator<Map<String, Vote>> iterator = row.values().iterator();
        Map<String, Vote> voteMap = null;
        long voteHeight = -1;
        while (iterator.hasNext()) {
            voteMap = iterator.next();
            LOGGER.info("the vote of version one is {}", voteMap);
            if (voteMap == null || voteMap.size() != 1) {
                continue;
            }
            Collection<Vote> values = voteMap.values();
            for (Vote vote : values) {
                if (vote != null) {
                    voteHeight = vote.getHeight();
                    if (voteHeight != -1) {
                        return voteHeight;
                    }
                }
            }
        }
        return voteHeight;
    }
}
