package cn.primeledger.cas.global.consensus;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yuguojia
 * @date 2018/03/02
 **/
@Service
@Data
public class ScoreManager {
    /**
     * key:address, value:score
     * when current block height is [1-100], it stores the score of  [1-100]
     * when current block height is [101-200], it stores the score of  [1-100]
     * when current block height is [201-300], it stores the score of  [1-100]
     * when current block height is [301-400], it stores the score of  [1-200]
     * when current block height is [401-500], it stores the score of  [1-300]
     * and so on
     */
    private Map<String, Integer> dposBaseMinerSoreMap = new ConcurrentHashMap<>(256);

    private Map<String, Integer> tmpMinerSoreMap = new ConcurrentHashMap<>(256);

    /**
     * key:address, value:score
     * all of scores from genesis block to current block
     */
    private Map<String, Integer> allMinerSoreMap = new ConcurrentHashMap<>(256);

    public Integer findDposScore(String address) {
        return dposBaseMinerSoreMap.get(address);
    }

    public void freshDposScoreMap() {
        Map<String, Integer> newBaseMinerSoreMap = new ConcurrentHashMap<>();
        if (tmpMinerSoreMap.isEmpty()) {
            tmpMinerSoreMap.putAll(allMinerSoreMap);
        }
        newBaseMinerSoreMap.putAll(tmpMinerSoreMap);
        dposBaseMinerSoreMap = newBaseMinerSoreMap;

        tmpMinerSoreMap.clear();
        tmpMinerSoreMap.putAll(allMinerSoreMap);
    }

    public void putIfAbsent(String address, Integer score) {
        allMinerSoreMap.putIfAbsent(address, score);
    }

    public Integer get(String address) {
        return allMinerSoreMap.get(address);
    }

    public void put(String address, Integer score) {
        allMinerSoreMap.put(address, score);
    }

    public synchronized void updateNewScore(String oldAddress, String newAddress, Integer score) {
        remove(oldAddress);
        putIfAbsent(newAddress, score);
    }

    public void remove(String address) {
        allMinerSoreMap.remove(address);
    }

    public boolean isContains(String address) {
        return allMinerSoreMap.containsKey(address);
    }
}