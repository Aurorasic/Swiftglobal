package com.higgsblock.global.chain.app.consensus;

import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
//    private Map<String, Integer> dposMinerSoreMap = new ConcurrentHashMap<>(256);
//
//    private Map<String, Integer> tmpMinerSoreMap = new ConcurrentHashMap<>(256);

    /**
     * key:address, value:score
     * all of scores from genesis block to current block
     */
//    private Map<String, Integer> allMinerSoreMap = new ConcurrentHashMap<>(256);

    public static String DPOS_SCORE_KEY = "dposMinerSoreMap";
    public static String TMP_SCORE_KEY = "tmpMinerSoreMap";
    public static String ALL_SCORE_KEY = "allMinerSoreMap";

    @Resource(name = "minerScoreMaps")
    private ConcurrentMap<String, Map> minerScoreMaps;

    public Map getDposMinerSoreMap() {
        return minerScoreMaps.get("dposMinerSoreMap");
    }

    public void freshDposScoreMap() {
        Map allMinerSoreMap = minerScoreMaps.get("allMinerSoreMap");
        Map tmpMinerSoreMap = minerScoreMaps.get("tmpMinerSoreMap");

        Map<String, Integer> newBaseMinerSoreMap = new ConcurrentHashMap<>();
        if (tmpMinerSoreMap.isEmpty()) {
            tmpMinerSoreMap.putAll(allMinerSoreMap);
        }
        newBaseMinerSoreMap.putAll(tmpMinerSoreMap);
        Map dposMinerSoreMap = newBaseMinerSoreMap;

        tmpMinerSoreMap.clear();
        tmpMinerSoreMap.putAll(allMinerSoreMap);

        minerScoreMaps.put("allMinerSoreMap", allMinerSoreMap);
        minerScoreMaps.put("tmpMinerSoreMap", tmpMinerSoreMap);
        minerScoreMaps.put("dposMinerSoreMap", dposMinerSoreMap);
    }

    public void putIfAbsent(String address, Integer score) {
        Map<String, Integer> allMinerSoreMap = (Map<String, Integer>) minerScoreMaps.get("allMinerSoreMap");
        allMinerSoreMap.putIfAbsent(address, score);
        minerScoreMaps.put("allMinerSoreMap", allMinerSoreMap);
    }

    public Integer get(String address) {
        Map<String, Integer> allMinerSoreMap = (Map<String, Integer>) minerScoreMaps.get("allMinerSoreMap");
        return allMinerSoreMap.get(address);
    }

    public void put(String address, Integer score) {
        Map allMinerSoreMap = minerScoreMaps.get("allMinerSoreMap");
        allMinerSoreMap.put(address, score);
        minerScoreMaps.put("allMinerSoreMap", allMinerSoreMap);
    }

    public void remove(String address) {
        Map allMinerSoreMap = minerScoreMaps.get("allMinerSoreMap");
        Map tmpMinerSoreMap = minerScoreMaps.get("tmpMinerSoreMap");
        allMinerSoreMap.remove(address);
        tmpMinerSoreMap.remove(address);
        minerScoreMaps.put("allMinerSoreMap", allMinerSoreMap);
        minerScoreMaps.put("tmpMinerSoreMap", tmpMinerSoreMap);
    }

    /**
     public void freshDposScoreMap() {
     Map<String, Integer> newBaseMinerSoreMap = new ConcurrentHashMap<>();
     if (tmpMinerSoreMap.isEmpty()) {
     tmpMinerSoreMap.putAll(allMinerSoreMap);
     }
     newBaseMinerSoreMap.putAll(tmpMinerSoreMap);
     dposMinerSoreMap = newBaseMinerSoreMap;

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

     public void remove(String address) {
     allMinerSoreMap.remove(address);
     tmpMinerSoreMap.remove(address);
     }
     */
}