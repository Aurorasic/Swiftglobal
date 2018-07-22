package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockFullInfo;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component("blockHandler")
@Slf4j
public class BlockHandler extends BaseMessageHandler<Block> {
    @Autowired
    private IBlockChainService blockChainService;

    @Autowired
    private BlockService blockService;
    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Override
    protected boolean check(SocketRequest<Block> request) {
        Block block = request.getData();
        String hash = block.getHash();

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

        //4. check: producer stake
        boolean producerValid = blockChainService.checkBlockProducer(block);
        if (!producerValid) {
            LOGGER.error("the block produce stack is error: ", block.getSimpleInfo());
            return false;
        }

        //5.check: witness signatures
        boolean validWitnessSignature = blockChainService.checkWitnessSignature(block);
        if (!validWitnessSignature) {
            LOGGER.error("the block witness sig is error: ", block.getSimpleInfo());
            return false;
        }

        //6.check: orphan block
        boolean isOrphanBlock = blockChainService.isExistPreBlock(hash);
        if (isOrphanBlock) {
            BlockFullInfo blockFullInfo = new BlockFullInfo(block.getVersion(), request.getSourceId(), block);
            orphanBlockCacheManager.putAndRequestPreBlocks(blockFullInfo);
            LOGGER.warn("it is orphan block: ", block.getSimpleInfo());
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
    protected void process(SocketRequest<Block> request) {
        Block data = request.getData();
        long height = data.getHeight();
        String hash = data.getHash();
        boolean success = blockService.persistBlockAndIndex(data, data.getVersion());

        LOGGER.info("persisted block all info, success={},height={},block={}", success, height, hash);
    }
}
