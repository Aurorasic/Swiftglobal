package com.higgsblock.global.chain.app.blockchain.consensus.vote;

import com.higgsblock.global.chain.app.common.constants.EntityType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author yuanjiantao
 * @date 6/28/2018
 */
@Message(EntityType.VOTES_NOTIFY)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class VoteTableNotify extends BaseSerializer {

    private int version = 0;

    private Map<Integer, Map<String, Map<String, Vote>>> voteTable;

    public VoteTableNotify(Map<Integer, Map<String, Map<String, Vote>>> voteTable) {
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
        Vote vote = voteMap.values().stream().findFirst().get();
        height = vote.getHeight();
        return height;
    }

    public int getVersionSize() {
        if (MapUtils.isEmpty(voteTable)) {
            return 0;
        }
        return voteTable.size();
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

    public Map<Integer, Map<String, Vote>> getVoteMapOfVersionByPubKey(String pubKey) {
        Map<Integer, Map<String, Vote>> voteMapOfVersionByPubKey = new HashMap<>();
        if (!valid()) {
            throw new RuntimeException("the voteTable is not valid");
        }
        Set<Map.Entry<Integer, Map<String, Map<String, Vote>>>> entries = voteTable.entrySet();
        entries.parallelStream().forEach(integerMapEntry -> {
            Integer version = integerMapEntry.getKey();
            Map<String, Map<String, Vote>> voteMapOfVersion = integerMapEntry.getValue();
            if (MapUtils.isEmpty(voteMapOfVersion)) {
                return;
            }
            voteMapOfVersion.forEach((votePubKey, voteMap) -> {
                if (StringUtils.equals(votePubKey, pubKey)) {
                    voteMapOfVersionByPubKey.computeIfAbsent(version, (tmpVersion) -> new HashMap()).putAll(voteMap);
                }
            });
        });
        Iterator<Map.Entry<Integer, Map<String, Map<String, Vote>>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Map<String, Map<String, Vote>>> next = iterator.next();
        }
        return voteMapOfVersionByPubKey;
    }

    public Map<String, Vote> getVoteMap(String pubKey, int version) {
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
        boolean result = false;

        return result;
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
