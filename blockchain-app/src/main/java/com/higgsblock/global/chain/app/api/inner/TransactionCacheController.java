package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Chen Jiawei
 * @date 2018-10-25
 */
@RequestMapping("/TransactionCache")
@RestController
@Slf4j
public class TransactionCacheController {
    @Autowired
    private TransactionCacheManager txCacheManager;

    @RequestMapping("/list")
    public ConcurrentMap<String, Transaction> list() {
        return txCacheManager.getTransactionMap().asMap();
    }
}
