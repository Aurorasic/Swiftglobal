package cn.primeledger.cas.global.blockchain.handler;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockCacheManager;
import cn.primeledger.cas.global.blockchain.BlockFullInfo;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.common.handler.BroadcastEntityHandler;
import cn.primeledger.cas.global.constants.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component("blockHandler")
@Slf4j
public class BlockHandler extends BroadcastEntityHandler<Block> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockCacheManager blockCacheManager;

    @Override
    public EntityType getType() {
        return EntityType.BLOCK_BROADCAST;
    }

    //todo yuguojia remove synchronized
    @Override
    synchronized public void process(Block data, short version, String sourceId) {
        //todo yangyi 需要实现防攻击，避免大量的孤儿区块
        Block preBlock = blockService.getBlock(data.getPrevBlockHash());
        if (preBlock == null) {
            LOGGER.warn("cannot get pre block of {}, to cache", data.getHeight() + "_" + data.getHash());
            BlockFullInfo blockFullInfo = new BlockFullInfo(version, sourceId, data);
            blockCacheManager.put(blockFullInfo);
            return;
        }
        if (!blockService.validBasic(data)) {
            LOGGER.error("error block: " + data);
            return;
        }
        if (!blockService.validBlockTransactions(data)) {
            LOGGER.error("error block transactions: " + data);
            return;
        }
        boolean success = blockService.persistBlockAndIndex(data, sourceId, version);
        LOGGER.info("persist block {}", success);
        if (success && !data.isgenesisBlock()) {
            blockService.broadCastBlock(data);
        }
    }

    @Override
    synchronized public void queueElementConsumeOver() {
        blockCacheManager.fetchPreBlocks();
    }
}
