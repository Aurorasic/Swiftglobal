package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VotingBlockResponse;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.event.SyncBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.IVoteService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 5/25/2018
 */
@Slf4j
@Component
public class VotingBlockResponseHandler extends BaseMessageHandler<VotingBlockResponse> {

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

    @Override
    protected boolean valid(IMessage<VotingBlockResponse> message) {
        VotingBlockResponse votingBlockResponse = message.getData();
        if (null == votingBlockResponse || null == votingBlockResponse.getBlock()) {
            return false;
        }
        Block block = votingBlockResponse.getBlock();
        long height = block.getHeight();
        String blockHash = block.getHash();
        LOGGER.info("Received OriginalBlock height={},hash={}", height, blockHash);
        if (!block.valid()) {
            LOGGER.info("this block is not valid, height={}, hash={}", height, blockHash);
            return false;
        }
        return true;
    }

    @Override
    protected void process(IMessage<VotingBlockResponse> message) {
        if (!witnessService.isWitness(keyPair.getAddress())) {
            return;
        }
        VotingBlockResponse votingBlockResponse = message.getData();
        Block block = votingBlockResponse.getBlock();
        String sourceId = message.getSourceId();
        long height = block.getHeight();
        String prevBlockHash = block.getPrevBlockHash();
        String blockHash = block.getHash();
        int minTransactionNum = BlockService.MINIMUM_TRANSACTION_IN_BLOCK;
        if (block.getTransactions().size() < minTransactionNum) {
            LOGGER.info("transactions is less than {}, height={}, hash={}", minTransactionNum, height, blockHash);
            return;
        }
        if (voteService.isExist(height, blockHash)) {
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
            LOGGER.info("the prev block is not on the chain, height={}, hash={},prevHash={} ", height, blockHash, prevBlockHash);
            long orphanBlockHeight = height - 1L;
            eventBus.post(new SyncBlockEvent(orphanBlockHeight, prevBlockHash, sourceId));
            voteService.addOriginalBlockToCache(block);
            return;
        }
        LOGGER.info("check the VotingBlock success,add VotingBlock height={}, hash={}", height, blockHash);
        voteService.addVotingBlock(block);
        messageCenter.dispatchToWitnesses(votingBlockResponse);
    }
}
