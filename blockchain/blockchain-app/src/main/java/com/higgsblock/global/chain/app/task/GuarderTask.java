package com.higgsblock.global.chain.app.task;

import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.OriginBlockProcessor;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXOProcessor;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.service.impl.BlockIndexService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-17 10:44
 **/
@Component
@Slf4j
public class GuarderTask extends BaseTask implements IEventBusListener {
    private static long currHeight = 0;
    private static final long WAIT_MINER_TIME = 30;
    private static long curSec = 0;
    private static final long TASK_TIME = 5;


    @Autowired
    private BlockProcessor blockProcessor;
    @Autowired
    private OriginBlockProcessor sourceBlockService;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private UTXOProcessor utxoProcessor;
    @Autowired
    private BlockIndexService blockIndexService;

    @Subscribe
    public void process(BlockPersistedEvent event) {
        long maxHeight = event.getHeight();
        if (maxHeight > currHeight) {
            currHeight = maxHeight;
            curSec = 0;
        }
    }

    /**
     * Task.
     */
    @Override
    protected void task() {
        curSec += TASK_TIME;
        LOGGER.info("curSec={} currHeight={}", curSec, currHeight);
        if (curSec >= WAIT_MINER_TIME) {
            doMing();
        }
    }

    /**
     * Gets period ms.
     *
     * @return the period ms
     */
    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(TASK_TIME);
    }


    public void doMing() {
        long expectHeight = currHeight + 1;
        try {
            BlockIndex maxBlockIndex = blockIndexService.getBlockIndexByHeight(currHeight);
            if (maxBlockIndex == null) {
                LOGGER.warn("the blockIndex not found ,current height={}", currHeight);
                return;
            }
            for (String blockHash : maxBlockIndex.getBlockHashs()) {
                String address = peerManager.getSelf().getId();
                if (!utxoProcessor.hasStakeOnUnion(blockHash, address, SystemCurrencyEnum.GUARDER)) {
                    LOGGER.warn("this miner no guarder currency");
                    return;
                }
                LOGGER.info("begin to packageNewBlock,height={},preBlcokHash={},this guarder address ={}", expectHeight, blockHash, address);
                Block block = blockProcessor.packageNewBlock(blockHash);
                if (block == null) {
                    LOGGER.warn("can not produce a new block,height={},preBlcokHash={}", expectHeight, blockHash);
                    return;
                }
                long maxHeight = blockProcessor.getMaxHeight();
                if (block.getHeight() <= maxHeight) {
                    LOGGER.warn("the expect block height={}, but max height={}", block.getHeight(), maxHeight);
                    return;
                }

                if (expectHeight != block.getHeight()) {
                    LOGGER.warn("the expect height={}, but block height={}", expectHeight, block.getHeight());
                    return;
                }
                sourceBlockService.sendBlockToWitness(block);
            }
        } catch (Exception e) {
            LOGGER.error("doming exception,height={}", expectHeight, e);
        }
        LOGGER.warn("can not produce a new block,height={}", expectHeight);
    }
}