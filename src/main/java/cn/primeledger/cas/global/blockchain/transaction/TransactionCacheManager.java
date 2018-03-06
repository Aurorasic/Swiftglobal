package cn.primeledger.cas.global.blockchain.transaction;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * transaction cache manager that manage transaction for mining
 *
 * @author yuguojia
 * @date 2018/02/28
 **/
@Service
@Data
public class TransactionCacheManager {
    private Map<String, BaseTx> transactionMap = new HashMap(16);

    public boolean hasTx() {
        if (CollectionUtils.isNotEmpty(transactionMap.values())) {
            return true;
        }
        return false;
    }

    public void addTransaction(BaseTx transaction) {
        if (transactionMap == null) {
            transactionMap = new HashMap(16);
        }
        if (transaction == null) {
            throw new RuntimeException("transaction is null, cannot add to cache");
        }
        transactionMap.putIfAbsent(transaction.getHash(), transaction);
    }

    public void remove(String transactionHash) {
        if (transactionMap != null) {
            transactionMap.remove(transactionHash);
        }
    }

    public boolean isContains(String transactionHash) {
        if (transactionMap != null) {
            return transactionMap.containsKey(transactionHash);
        }
        return false;
    }
}