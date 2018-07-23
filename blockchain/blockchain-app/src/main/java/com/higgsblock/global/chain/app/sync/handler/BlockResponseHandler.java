package com.higgsblock.global.chain.app.sync.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockFullInfo;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.app.sync.message.BlockResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component
@Slf4j
public class BlockResponseHandler extends BaseMessageHandler<BlockResponse> {

    @Autowired
    private IBlockChainService blockChainService;

    @Autowired
    private BlockService blockService;
    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Override
    protected boolean check(SocketRequest<BlockResponse> request) {
        BlockResponse response = request.getData();
        Block block = response.getBlock();
        if (null == block) {
            return false;
        }
        String hash = block.getHash();
        String prevBlockHash = block.getPrevBlockHash();
        //1. check: isGenesisBlock
        boolean isGenesisBlock = blockChainService.isGenesisBlock(block);
        if (isGenesisBlock) {
            return false;
        }

        //2. check: base info
        boolean isBasicValid = blockChainService.checkBlockBasicInfo(block);
        if (!isBasicValid) {
            LOGGER.error("error basic info block: ", block.getSimpleInfo());
            return false;
        }

        //3.check: exist
        boolean isExist = orphanBlockCacheManager.isContains(hash) || blockChainService.isExistBlock(hash);
        if (isExist) {
            LOGGER.info("the block is exist: ", block.getSimpleInfo());
            return false;
        }


        //4.check: witness signatures
        boolean validWitnessSignature = blockChainService.checkWitnessSignature(block);
        if (!validWitnessSignature) {
            LOGGER.error("the block witness sig is error: ", block.getSimpleInfo());
            return false;
        }

        //5.check: orphan block
        boolean isOrphanBlock = blockChainService.isExistBlock(prevBlockHash);
        if (isOrphanBlock) {
            BlockFullInfo blockFullInfo = new BlockFullInfo(block.getVersion(), request.getSourceId(), block);
            orphanBlockCacheManager.putAndRequestPreBlocks(blockFullInfo);
            LOGGER.warn("it is orphan block: ", block.getSimpleInfo());
            return false;
        }

        //6. check: producer stake
        boolean producerValid = blockChainService.checkBlockProducer(block);
        if (!producerValid) {
            LOGGER.error("the block produce stack is error: ", block.getSimpleInfo());
            return false;
        }

        //7. check: transactions
        boolean validTransactions = blockChainService.checkTransactions(block);
        if (!validTransactions) {
            LOGGER.error("the block transactions are error: ", block.getSimpleInfo());
            return false;
        }
        return true;
    }

    @Override
    protected void process(SocketRequest<BlockResponse> request) {
        BlockResponse blockResponse = request.getData();
        Block data = blockResponse.getBlock();
        long height = data.getHeight();
        String hash = data.getHash();
        boolean success = blockService.persistBlockAndIndex(data);

        LOGGER.info("persisted block all info, success={},height={},block={}", success, height, hash);
    }
}
