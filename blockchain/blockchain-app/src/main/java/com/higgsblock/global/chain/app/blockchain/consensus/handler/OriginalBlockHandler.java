package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.WitnessTimer;
import com.higgsblock.global.chain.app.blockchain.consensus.message.OriginalBlock;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
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
    protected boolean check(SocketRequest<OriginalBlock> request) {
        OriginalBlock originalBlock = request.getData();
        String sourceId = request.getSourceId();
        Block block;
        LOGGER.info("Received OriginalBlock {}", request);
        if (null == originalBlock || null == (block = originalBlock.getBlock())) {
            return false;
        }
        long height = block.getHeight();
        String prevBlockHash = block.getPrevBlockHash();
        String blockHash = block.getHash();
        String pubKey = block.getPubKey();
        if (!block.valid()) {
            LOGGER.info("this block is not valid, height={}, hash={}", height, blockHash);
            return false;
        }
        int minTransactionNum = BlockService.MINIMUM_TRANSACTION_IN_BLOCK;
        if (block.getTransactions().size() < minTransactionNum) {
            LOGGER.error("transactions is less than {}, height={}, hash={}", minTransactionNum, height, blockHash);
            return false;
        }
        if (voteService.isExistInBlockCache(height, blockHash)) {
            LOGGER.info("this block is exist in block cache, height={}, hash={}", height, blockHash);
            return false;
        }
        if (blockChainService.isExistBlock(blockHash)) {
            LOGGER.error("the block is already on the chain, height={}, hash={}", height, blockHash);
            return false;
        }
        long maxHeight = blockChainService.getMaxHeight();
        if (height <= maxHeight) {
            LOGGER.error("the height is already on the chain, height={}, hash={}", height, blockHash);
            return false;
        }
        if (!blockChainService.isExistBlock(prevBlockHash)) {
            LOGGER.error("the prev block is not on the chain, height={}, hash={},prevHash", height, blockHash, prevBlockHash);
            long orphanBlockHeight = height - 1L;
            eventBus.post(new ReceiveOrphanBlockEvent(orphanBlockHeight, prevBlockHash, sourceId));
            return false;
        }
        boolean isDposMiner = blockChainService.isDposMiner(ECKey.pubKey2Base58Address(pubKey), prevBlockHash);
        if (!isDposMiner) {
            LOGGER.info("this miner can not package the height, height={}, hash={}", height, blockHash);
            boolean acceptBlock = witnessTimer.acceptBlock(block);
            if (!acceptBlock) {
                LOGGER.info("can not accept this block, height={}, hash={}", height, blockHash);
                return false;
            }
        }
        if (!blockChainService.checkTransactions(block)) {
            LOGGER.error("the transactions are not valid, height={}, hash={}", height, blockHash);
            return false;
        }
        LOGGER.info("check the OriginalBlock success, height={}, hash={}", height, blockHash);
        return true;
    }

    @Override
    protected void process(SocketRequest<OriginalBlock> request) {
        OriginalBlock originalBlock = request.getData();
        Block block = originalBlock.getBlock();
        if (!witnessService.isWitness(keyPair.getAddress())) {
            messageCenter.dispatchToWitnesses(originalBlock);
            return;
        }
        LOGGER.info("add OriginalBlock height={}, hash={}", block.getHeight(), block.getHash());
        voteService.addOriginalBlock(block);
        messageCenter.dispatchToWitnesses(originalBlock);
    }
}
