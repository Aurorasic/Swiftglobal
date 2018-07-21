package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.app.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author yuguojia
 * @date 2018-07-20
 */
public class BlockChainService implements IBlockChainService {

    @Autowired
    private IBlockService blockService;

    @Autowired
    private IBlockIndexService blockIndexService;

    @Autowired
    private ITransactionService transactionService;

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
        boolean result = transactionService.validTransactions(block);
        return result;
    }

    @Override
    public boolean checkWitnessSignature(Block block) {
        boolean result = blockService.checkWitnessSignatures(block);
        return result;
    }

    @Override
    public boolean checkBlockProducer(Block block) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public long getMaxHeight() {
        BlockIndex index = blockIndexService.getLastBlockIndex();
        long height = index == null ? 0 : index.getHeight();
        return height;
    }

    @Override
    public long getBestMaxHeight() {
        long height = blockService.getLastBestBlockIndex().getHeight();
        return height;
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
