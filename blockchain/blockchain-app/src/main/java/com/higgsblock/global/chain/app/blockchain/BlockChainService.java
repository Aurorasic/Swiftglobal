package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.service.impl.BlockIndexService;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author yuguojia
 * @date 2018-07-20
 */
public class BlockChainService implements IBlockChain {

    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockIndexService blockIndexService;

    @Override
    public boolean isLuckyMiner(String address, long height) {

    }

    @Override
    public boolean isMiner(String address, long height) {

    }

    @Override
    public boolean isWitness(String address, long height) {

    }

    @Override
    public boolean isGuarder(String address, long height) {

    }

    @Override
    public boolean isGenesisBlock(Block block) {

    }

    @Override
    public boolean isGenesisBlock(String blockHash) {

    }

    @Override
    public boolean isExistPreBlock(String blockHash) {

    }

    @Override
    public boolean isExistBlock(String blockHash) {

    }

    @Override
    public boolean checkBlockBasicInfo(Block block) {
        return block.valid();
    }

    @Override
    public boolean checkTransactions(Block block) {

    }

    @Override
    public boolean checkWitnessSignature(Block block) {

    }

    @Override
    public boolean checkBlockProducer(Block block) {

    }

    @Override
    public long getMaxHeight() {

    }

    @Override
    public long getBestMaxHeight() {

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
    public List<Block> getHeightestBlocks() {
        BlockIndex lastBlockIndex = blockIndexService.getLastBlockIndex();
        long height = lastBlockIndex.getHeight();
        return getBlocks(height);
    }
}
