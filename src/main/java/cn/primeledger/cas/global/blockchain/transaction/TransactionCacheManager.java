package cn.primeledger.cas.global.blockchain.transaction;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * transaction cache manager that manage transaction for mining
 *
 * @author yuguojia
 * @date 2018/02/28
 **/
@Service
@Data
public class TransactionCacheManager {

    private static final int MAX_SIZE = 10000;

    private Cache<String, Transaction> transactionMap = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();

//    private Map<String, Transaction> transactionMap = new HashMap(16);

    public boolean hasTx() {
        if (CollectionUtils.isNotEmpty(transactionMap.asMap().values())) {
            return true;
        }
        return false;
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new RuntimeException("transaction is null, cannot add to cache");
        }
        transactionMap.put(transaction.getHash(), transaction);
    }

    public void remove(String transactionHash) {
        if (transactionMap != null) {
            transactionMap.invalidate(transactionHash);
        }
    }

    public boolean isContains(String transactionHash) {
        if (transactionMap != null) {
            return transactionMap.asMap().containsKey(transactionHash);
        }
        return false;
    }
}