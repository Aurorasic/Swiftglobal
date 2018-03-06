package cn.primeledger.cas.global.blockchain;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * orphan blocks manager storing blocks cannot been connected on chain
 *
 * @author yuguojia
 * @date 2018/2/28
 **/
@Service
@Data
public class BlockCacheManager {
    private Map<String, Block> orphanBlockMap;

    public void addBlock(Block block) {
        if (orphanBlockMap == null) {
            orphanBlockMap = new HashMap(16);
        }
        orphanBlockMap.putIfAbsent(block.getHash(), block);
    }

    public void remove(String blockHash) {
        if (orphanBlockMap != null) {
            orphanBlockMap.remove(blockHash);
        }
    }

    public boolean isContains(String blockHash) {
        if (orphanBlockMap != null) {
            return orphanBlockMap.containsKey(blockHash);
        }
        return false;
    }
}