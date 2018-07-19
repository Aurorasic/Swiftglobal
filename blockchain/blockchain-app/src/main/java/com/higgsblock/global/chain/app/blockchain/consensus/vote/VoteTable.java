package com.higgsblock.global.chain.app.blockchain.consensus.vote;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
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

    private Map<Integer, Map<String, Map<String, Vote>>> voteTable;

    public VoteTable(Map<Integer, Map<String, Map<String, Vote>>> voteTable) {
        this.voteTable = voteTable;
    }

    public long getVoteHeight() {
        long height;
        if (!valid()) {
            throw new RuntimeException("the voteTable is not valid");
        }
        Map<String, Map<String, Vote>> firstVersionVoteMap = voteTable.get(1);
        if (MapUtils.isEmpty(firstVersionVoteMap)) {
            throw new RuntimeException("the voteTable hasn't vote of version one");
        }
        Map<String, Vote> voteMap = firstVersionVoteMap.values().stream().findAny().get();
        if (MapUtils.isEmpty(voteMap)) {
            throw new RuntimeException("the voteMap of version one is empty");
        }
        Vote vote = voteMap.values().stream().findAny().get();
        height = vote.getHeight();
        return height;
    }

    public int getVersionSize() {
        if (!valid()) {
            return 0;
        }
        return voteTable.size();
    }

    public int getAllVoteSize() {
        if (!valid()) {
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
        if (!valid()) {
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
        Map<String, Map<String, Vote>> result = new HashMap<>();
        if (!valid()) {
            throw new RuntimeException("the voteTable is not valid");
        }
        Map<String, Map<String, Vote>> voteMapOfPubKey = voteTable.get(version);
        if (MapUtils.isEmpty(voteMapOfPubKey)) {
            return result;
        }
        result.putAll(voteMapOfPubKey);
        return result;
    }

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

    public boolean addVote(Vote vote) {
        if (!valid() || vote == null) {
            return false;
        }
        String pubKey = vote.getWitnessPubKey();
        int version = vote.getVoteVersion();
        String blockHash = vote.getBlockHash();
        Map<String, Map<String, Vote>> versionVoteMap = voteTable.computeIfAbsent(version, (key) -> new HashMap());
        Map<String, Vote> pubKeyVoteMap = versionVoteMap.computeIfAbsent(pubKey, (key) -> new HashMap());
        Vote old = pubKeyVoteMap.computeIfAbsent(blockHash, (hash) -> vote);
        return old == null;
    }

    public boolean valid() {
        if (version < 0) {
            return false;
        }
        if (MapUtils.isEmpty(voteTable)) {
            return false;
        }
        return true;
    }


}
