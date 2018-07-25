package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.WitnessTimer;
import com.higgsblock.global.chain.app.blockchain.consensus.message.OriginalBlock;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.event.SyncBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import com.higgsblock.global.chain.app.service.IVoteService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yangyi
 * @deta 2018/7/19
 * @description
 */
@Slf4j
@Component
public class OriginalBlockHandler extends BaseMessageHandler<OriginalBlock> {

    @Autowired
    private IVoteService voteService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private IWitnessService witnessService;

    @Autowired
    private IBlockChainService blockChainService;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private WitnessTimer witnessTimer;

    @Override
    protected boolean valid(IMessage<OriginalBlock> message) {
        OriginalBlock originalBlock = message.getData();
        LOGGER.info("Received OriginalBlock {}", message);
        if (null == originalBlock || null == originalBlock.getBlock()) {
            return false;
        }
        Block block = originalBlock.getBlock();
        long height = block.getHeight();
        String blockHash = block.getHash();
        if (!block.valid()) {
            LOGGER.info("this block is not valid, height={}, hash={}", height, blockHash);
            return false;
        }
        return true;
    }

    @Override
    protected void process(IMessage<OriginalBlock> message) {
        OriginalBlock originalBlock = message.getData();
        Block block = originalBlock.getBlock();
        String sourceId = message.getSourceId();
        long height = block.getHeight();
        String prevBlockHash = block.getPrevBlockHash();
        String blockHash = block.getHash();
        String pubKey = block.getPubKey();
        int minTransactionNum = BlockService.MINIMUM_TRANSACTION_IN_BLOCK;
        if (block.getTransactions().size() < minTransactionNum) {
            LOGGER.info("transactions is less than {}, height={}, hash={}", minTransactionNum, height, blockHash);
            return;
        }
        if (voteService.isExistInBlockCache(height, blockHash)) {
            LOGGER.info("this block is exist in block cache, height={}, hash={}", height, blockHash);
            return;
        }
        if (blockChainService.isExistBlock(blockHash)) {
            LOGGER.info("the block is already on the chain, height={}, hash={}", height, blockHash);
            return;
        }
        long maxHeight = blockChainService.getMaxHeight();
        if (height <= maxHeight) {
            LOGGER.info("the height is already on the chain, height={}, hash={}", height, blockHash);
            return;
        }
        if (!blockChainService.isExistBlock(prevBlockHash)) {
            LOGGER.info("the prev block is not on the chain, height={}, hash={},prevHash", height, blockHash, prevBlockHash);
            long orphanBlockHeight = height - 1L;
            eventBus.post(new SyncBlockEvent(orphanBlockHeight, prevBlockHash, sourceId));
            return;
        }
        boolean isDposMiner = blockChainService.isDposMiner(ECKey.pubKey2Base58Address(pubKey), prevBlockHash);
        if (!isDposMiner) {
            LOGGER.error("this miner can not package the height, height={}, hash={}", height, blockHash);
            boolean acceptBlock = witnessTimer.acceptBlock(block);
            if (!acceptBlock) {
                LOGGER.error("can not accept this block, height={}, hash={}", height, blockHash);
                return;
            }
        }
        if (!blockChainService.checkTransactions(block)) {
            LOGGER.error("the transactions are not valid, height={}, hash={}", height, blockHash);
            return;
        }
        LOGGER.info("check the OriginalBlock success, height={}, hash={}", height, blockHash);
        if (!witnessService.isWitness(keyPair.getAddress())) {
            messageCenter.dispatchToWitnesses(originalBlock);
            return;
        }
        LOGGER.info("add OriginalBlock height={}, hash={}", block.getHeight(), block.getHash());
        voteService.addOriginalBlock(block);
        messageCenter.dispatchToWitnesses(originalBlock);
    }
}
