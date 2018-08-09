package com.higgsblock.global.chain.app.blockchain.consensus.message;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.blockchain.consensus.vote.Vote;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author yuanjiantao
 * @date 6/28/2018
 */
@Message(MessageType.VOTE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
@JSONType(includes = {"version", "height", "voteTable"})
public class VoteTable extends BaseSerializer {

    private int version = 0;

    private long height;

    private Map<Integer, Map<String, Map<String, Vote>>> voteTable;

    public VoteTable(Map<Integer, Map<String, Map<String, Vote>>> voteTable, long height) {
        this.voteTable = voteTable;
        this.height = height;
    }

    public int getVersionSize() {
        return MapUtils.isEmpty(voteTable) ? 0 : voteTable.size();
    }

    public int getAllVoteSize() {
        if (MapUtils.isEmpty(voteTable)) {
            return 0;
        }
        int size = 0;
        for (Map<String, Map<String, Vote>> versionVoteMap : voteTable.values()) {
            if (MapUtils.isEmpty(versionVoteMap)) {
                continue;
            }
            for (Map<String, Vote> pubKeyVoteMap : versionVoteMap.values()) {
                if (MapUtils.isEmpty(pubKeyVoteMap)) {
                    continue;
                }
                size = size + pubKeyVoteMap.size();
            }
        }
        return size;
    }

    public int getARowVoteSize(int version) {
        if (MapUtils.isEmpty(voteTable)) {
            return 0;
        }
        Map<String, Map<String, Vote>> versionVoteMap = voteTable.get(version);
        if (MapUtils.isEmpty(versionVoteMap)) {
            return 0;
        }
        int size = 0;
        for (Map<String, Vote> pubKeyVoteMap : versionVoteMap.values()) {
            if (MapUtils.isEmpty(pubKeyVoteMap)) {
                continue;
            }
            size = size + pubKeyVoteMap.size();
        }
        return size;
    }

    public Map<String, Map<String, Vote>> getVoteMapOfPubKeyByVersion(int version) {
        Map<String, Map<String, Vote>> result = Maps.newHashMap();
        if (MapUtils.isEmpty(voteTable)) {
            return result;
        }
        Map<String, Map<String, Vote>> voteMapOfPubKey = voteTable.get(version);
        if (MapUtils.isEmpty(voteMapOfPubKey)) {
            return result;
        }
        result.putAll(voteMapOfPubKey);
        return result;
    }

    public Map<String, Vote> getVoteMap(int version, String pubKey) {
        Map<String, Vote> result = Maps.newHashMap();
        Map<String, Map<String, Vote>> voteMapOfPubKey = getVoteMapOfPubKeyByVersion(version);
        if (MapUtils.isEmpty(voteMapOfPubKey)) {
            return result;
        }
        Map<String, Vote> voteMap = voteMapOfPubKey.get(pubKey);
        if (MapUtils.isEmpty(voteMap)) {
            return result;
        }
        result.putAll(voteMap);
        return result;
    }

    public void addVote(Vote vote) {
        if (voteTable == null || vote == null) {
            return;
        }
        String pubKey = vote.getWitnessPubKey();
        int version = vote.getVoteVersion();
        String blockHash = vote.getBlockHash();
        Map<String, Map<String, Vote>> versionVoteMap = voteTable.computeIfAbsent(version, (key) -> new HashMap<>(11));
        Map<String, Vote> pubKeyVoteMap = versionVoteMap.computeIfAbsent(pubKey, (key) -> new HashMap<>(2));
        pubKeyVoteMap.putIfAbsent(blockHash, vote);
    }

    public boolean valid() {
        if (height <= 1L || MapUtils.isEmpty(voteTable)) {
            return false;
        }

        Set<String> version1Witnesses = new HashSet<>(11);

        //check all votes
        for (int version = 1; version <= voteTable.size(); version++) {
            // check version
            if (!voteTable.containsKey(version)) {
                return false;
            }

            Map<String, Map<String, Vote>> map = voteTable.get(version);
            if (MapUtils.isEmpty(map)) {
                return false;
            }
            for (Map.Entry<String, Map<String, Vote>> entry : map.entrySet()) {
                String witnessPubKey = entry.getKey();
                Map<String, Vote> map1 = entry.getValue();
                //check witness
                if (MapUtils.isEmpty(map1)) {
                    return false;
                }
                //check every vote
                for (Vote vote : map1.values()) {

                    if (height != vote.getHeight()
                            || version != vote.getVoteVersion()
                            || !StringUtils.equals(vote.getWitnessPubKey(), witnessPubKey)) {
                        return false;
                    }

                    if (version == 1) {
                        version1Witnesses.add(witnessPubKey);
                    } else if (!version1Witnesses.contains(witnessPubKey)) {
                        return false;
                    }

                    if (!vote.valid()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
