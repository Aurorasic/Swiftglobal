package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.service.impl.BlockIndexService;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.app.service.impl.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author yuguojia
 * @date 2018-07-20
 */
public class BlockChainService implements IBlockChainService {

    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockIndexService blockIndexService;

    @Autowired
    private TransactionService transactionService;

    @Override
    public boolean isLuckyMiner(String address, String preBlockHash) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean isMinerOnBest(String address) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean isWitness(String address, long height) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean isGuarder(String address, String preBlockHash) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean isGenesisBlock(Block block) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean isGenesisBlock(String blockHash) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean isExistPreBlock(String blockHash) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean isExistBlock(String blockHash) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean checkBlockBasicInfo(Block block) {
        return block.valid();
    }

    @Override
    public boolean checkTransactions(Block block) {
        return transactionService.validTransactions(block);
    }

    @Override
    public boolean checkWitnessSignature(Block block) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean checkBlockProducer(Block block) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public long getMaxHeight() {
        //// TODO: 2018/7/20/0020
        return 0;
    }

    @Override
    public long getBestMaxHeight() {
        //// TODO: 2018/7/20/0020
        return 0;
    }

    @Override
    public Block getBlock(String blockHash) {
        return blockService.getBlockByHash(blockHash);
    }

    @Override
    public List<Block> getBlocks(long height) {
        return blockService.getBlocksByHeight(height);
    }

    @Override
    public BlockIndex getBlockIndex(long height) {
        return blockIndexService.getBlockIndexByHeight(height);
    }

    @Override
    public List<Block> getHighestBlocks() {
        BlockIndex lastBlockIndex = blockIndexService.getLastBlockIndex();
        long height = lastBlockIndex.getHeight();
        return getBlocks(height);
    }
}
