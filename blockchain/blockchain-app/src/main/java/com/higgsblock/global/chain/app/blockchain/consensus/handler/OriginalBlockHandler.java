package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.consensus.message.OriginalBlock;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.VoteProcessor;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.app.service.impl.BlockService;
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
    private VoteProcessor voteProcessor;

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
        String minerAddress = block.getPubKey();
        if (!block.valid()) {
            LOGGER.info("this block is not valid, height={}, hash={}", height, blockHash);
            return false;
        }
        int minTransactionNum = BlockService.MINIMUM_TRANSACTION_IN_BLOCK;
        if (block.getTransactions().size() < minTransactionNum) {
            LOGGER.error("transactions is less than {}, height={}, hash={}", minTransactionNum, height, blockHash);
            return false;
        }
        if (voteProcessor.isExistInBlockCache(height, blockHash)) {
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
        if (!blockChainService.isExistPreBlock(blockHash)) {
            LOGGER.error("the prev block is not on the chain, height={}, hash={},prevHash", height, blockHash, prevBlockHash);
            long orphanBlockHeight = height - 1L;
            eventBus.post(new ReceiveOrphanBlockEvent(orphanBlockHeight, prevBlockHash, sourceId));
            return false;
        }
        boolean isLuckyMiner = blockChainService.isLuckyMiner(minerAddress, prevBlockHash);
        if (!isLuckyMiner) {
            boolean isGuarder = blockChainService.isGuarder(minerAddress, prevBlockHash);
            if (!isGuarder) {
                return false;
            }
        }
        if (!blockChainService.checkWitnessSignature(block)) {
            LOGGER.error("the signature of all witnesses is not valid, height={}, hash={}", height, blockHash);
            return false;
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
        voteProcessor.addOriginalBlock(block);
        messageCenter.dispatchToWitnesses(originalBlock);
    }
}
