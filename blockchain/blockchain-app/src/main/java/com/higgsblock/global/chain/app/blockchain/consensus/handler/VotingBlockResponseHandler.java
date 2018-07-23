package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VotingBlockResponse;
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
    @Autowired
    private WitnessTimer witnessTimer;

    @Override
    protected boolean check(SocketRequest<VotingBlockResponse> request) {
        VotingBlockResponse votingBlockResponse = request.getData();
        String sourceId = request.getSourceId();
        Block block;
        LOGGER.info("Received VotingBlockResponse {}", request);
        if (null == votingBlockResponse || null == (block = votingBlockResponse.getBlock())) {
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
            LOGGER.error("this block is exist in block cache, height={}, hash={}", height, blockHash);
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
            LOGGER.error("the prev block is not on the chain, height={}, hash={},prevHash ", height, blockHash, prevBlockHash);
            long orphanBlockHeight = height - 1L;
            eventBus.post(new ReceiveOrphanBlockEvent(orphanBlockHeight, prevBlockHash, sourceId));
            return false;
        }
        boolean isLuckyMiner = blockChainService.isLuckyMiner(ECKey.pubKey2Base58Address(pubKey), prevBlockHash);
        if (!isLuckyMiner) {
            LOGGER.info("this miner can not package the height, height={}, hash={}", height, blockHash);
            boolean acceptBlock = witnessTimer.acceptBlock(block);
            if (!acceptBlock) {
                LOGGER.info("can not accept this block, height={}, hash={} ", height, blockHash);
                return false;
            }
        }
        if (!blockChainService.checkTransactions(block)) {
            LOGGER.error("the transactions are not valid, height={}, hash={}", height, blockHash);
            return false;
        }
        LOGGER.info("check the VotingBlockResponse success, height={}, hash={}", height, blockHash);
        return true;
    }

    @Override
    protected void process(SocketRequest<VotingBlockResponse> request) {
        VotingBlockResponse votingBlockResponse = request.getData();
        Block block = votingBlockResponse.getBlock();
        if (!witnessService.isWitness(keyPair.getAddress())) {
            messageCenter.dispatchToWitnesses(votingBlockResponse);
            return;
        }
        LOGGER.info("add VotingBlockResponse height={}, hash={}", block.getHeight(), block.getHash());
        voteService.addVotingBlock(block);
        messageCenter.dispatchToWitnesses(votingBlockResponse);
    }
}
