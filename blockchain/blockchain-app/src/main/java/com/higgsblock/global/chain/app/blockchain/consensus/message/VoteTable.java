package com.higgsblock.global.chain.app.blockchain.consensus.message;

import com.alibaba.fastjson.annotation.JSONField;
import com.higgsblock.global.chain.app.blockchain.consensus.vote.Vote;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuanjiantao
 * @date 6/28/2018
 */
@Message(MessageType.VOTE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class VoteTable extends BaseSerializer {

    private int version = 0;

    private long height;

    private Map<Integer, Map<String, Map<String, Vote>>> voteTable;

    public VoteTable(Map<Integer, Map<String, Map<String, Vote>>> voteTable, long height) {
        this.voteTable = voteTable;
        this.height = height;
    }

    @JSONField(serialize = false)
    public int getVersionSize() {
//        if (!valid()) {
//            return 0;
//        }
        return voteTable.size();
    }

    @JSONField(serialize = false)
    public int getAllVoteSize() {
//        if (!valid()) {
//            return 0;
//        }
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

    @JSONField(serialize = false)
    public int getARowVoteSize(int version) {
//        if (!valid()) {
//            return 0;
//        }
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

    @JSONField(serialize = false)
    public Map<String, Map<String, Vote>> getVoteMapOfPubKeyByVersion(int version) {
        Map<String, Map<String, Vote>> result = new HashMap<>();
//        if (!valid()) {
//            throw new RuntimeException("the voteTable is not valid");
//        }
        Map<String, Map<String, Vote>> voteMapOfPubKey = voteTable.get(version);
        if (MapUtils.isEmpty(voteMapOfPubKey)) {
            return result;
        }
        result.putAll(voteMapOfPubKey);
        return result;
    }

    @JSONField(serialize = false)
    public Map<String, Vote> getVoteMap(int version, String pubKey) {
        Map<String, Vote> result = new HashMap<>();
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

    public boolean valid(List<String> witnesses) {
        if (height <= 1L || MapUtils.isEmpty(voteTable)) {
            return false;
        }

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
                if (!witnesses.contains(ECKey.pubKey2Base58Address(witnessPubKey)) || MapUtils.isEmpty(map1)) {
                    return false;
                }
                //check every vote
                for (Vote vote : map1.values()) {
                    if (height != vote.getHeight()
                            || version != vote.getVoteVersion()
                            || !StringUtils.equals(vote.getWitnessPubKey(), witnessPubKey)) {
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
