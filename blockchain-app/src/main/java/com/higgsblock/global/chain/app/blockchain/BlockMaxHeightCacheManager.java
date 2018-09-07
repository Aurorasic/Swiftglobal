package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.dao.IBlockIndexRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yuguojia
 * @date 2018/08/06
 **/
@Deprecated
@Service
@Slf4j
public class BlockMaxHeightCacheManager {
    private Long lastBlockHeight;
    @Autowired
    private IBlockIndexRepository blockIndexRepository;

    public synchronized void updateMaxHeight(Long lastBlockHeight) {
        this.lastBlockHeight = lastBlockHeight;
    }

    public synchronized Long getMaxHeight() {
        if (lastBlockHeight == null) {
            loadMaxBlockHeight();
        }
        return lastBlockHeight;
    }

    private void loadMaxBlockHeight() {
        this.lastBlockHeight = blockIndexRepository.queryMaxHeight();
    }
}