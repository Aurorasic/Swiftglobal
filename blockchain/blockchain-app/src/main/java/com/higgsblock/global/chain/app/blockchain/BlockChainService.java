package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.app.service.IDposService;
import com.higgsblock.global.chain.app.service.ITransactionService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import org.apache.commons.collections.CollectionUtils;
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

    @Autowired
    private IDposService dposService;

    @Autowired
    private WitnessTimerProcessor witnessTimerProcessor;

    @Override
    public boolean isLuckyMiner(String address, String preBlockHash) {
        // get lucky miners address at branch which the preblock belonged to
        List<String> luckyAddresses = dposService.getDposGroupByHeihgt(preBlockHash);
        return CollectionUtils.isNotEmpty(luckyAddresses) && luckyAddresses.contains(address);
    }

    @Override
    public boolean isMinerOnBest(String address) {
        ////check the miner own the MINER
        return transactionService.hasStake(address, SystemCurrencyEnum.MINER);
    }

    @Override
    public boolean isWitness(String address, long height) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    public boolean isGuarder(String address, String preBlockHash) {
        return transactionService.hasStake(preBlockHash, address, SystemCurrencyEnum.GUARDER);
    }

    @Override
    public boolean isGenesisBlock(Block block) {
        boolean result = null != block && block.isGenesisBlock();
        return result;
    }

    @Override
    public boolean isGenesisBlock(String blockHash) {
        Block block = getBlock(blockHash);
        return isGenesisBlock(block);
    }

    @Override
    public boolean isExistPreBlock(String blockHash) {
        Block block = getBlock(blockHash);
        boolean result = blockService.preIsExistInDB(block);
        return result;
    }

    @Override
    public boolean isExistBlock(String blockHash) {
        Block block = blockService.getBlockByHash(blockHash);
        return null != block;
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
        boolean result = blockService.checkWitnessSignatures(block);
        return result;
    }

    @Override
    public boolean checkBlockProducer(Block block) {
        // 1.check the producer of the block.
        boolean result = blockService.checkBlockProducer(block);
        if (result) {
            return true;
        }

        // 2.check the guarder permission
        result = witnessTimerProcessor.acceptBlock(block);
        if (result) {
            return true;
        }

        return false;
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