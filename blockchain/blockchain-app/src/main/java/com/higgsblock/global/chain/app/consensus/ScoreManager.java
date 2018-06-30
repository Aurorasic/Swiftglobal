package com.higgsblock.global.chain.app.consensus;


import lombok.Data;
import org.springframework.stereotype.Service;
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

//    @Autowired
//    private ScoreDao scoreDao;


//    public Map getDposMinerSoreMap() {
//        return getAllScoreMap();
//    }
//
//    public Map getAllScoreMap() {
//        try {
//            return null == scoreDao.get("allMinerSoreMap") ? new HashMap<String, Integer>(10) : scoreDao.get("allMinerSoreMap");
//        } catch (RocksDBException e) {
//            throw new IllegalStateException("Get all score error");
//        }
//    }
//
//    public List<BaseDaoEntity> remove(Map<String, Integer> allMinerSoreMap, String address) throws RocksDBException {
//        List<BaseDaoEntity> entityList = new ArrayList<>();
//        allMinerSoreMap.remove(address);
//
//        BaseDaoEntity allMinerSoreEntity = scoreDao.getEntity("allMinerSoreMap", allMinerSoreMap);
//        entityList.add(allMinerSoreEntity);
//
//        return entityList;
//    }
//
//    public BaseDaoEntity putIfAbsent(Map<String, Integer> allMinerSoreMap, String address, Integer score) throws RocksDBException {
//        allMinerSoreMap.putIfAbsent(address, score);
//
//        BaseDaoEntity allMinerSoreEntity = scoreDao.getEntity("allMinerSoreMap", allMinerSoreMap);
//
//        return allMinerSoreEntity;
//    }
//
//    public Integer get(String address) throws RocksDBException {
//        Map<String, Integer> allMinerSoreMap = (Map<String, Integer>) scoreDao.get("allMinerSoreMap");
//
//        if (allMinerSoreMap == null) {
//            return null;
//        }
//        return allMinerSoreMap.get(address);
//    }
//
//    public BaseDaoEntity put(Map<String, Integer> allMinerSoreMap, String address, Integer score) throws RocksDBException {
//        if (allMinerSoreMap == null) {
//            return null;
//        }
//        allMinerSoreMap.put(address, score);
//
//        BaseDaoEntity allMinerSoreEntity = scoreDao.getEntity("allMinerSoreMap", allMinerSoreMap);
//
//        return allMinerSoreEntity;
//    }
}
