package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.service.*;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yuguojia
 * @date 2018-07-20
 */
@Service
@Slf4j
public class BlockChainService implements IBlockChainService {

    @Autowired
    private AppConfig config;

    @Autowired
    private IBlockService blockService;

    @Autowired
    private IBlockIndexService blockIndexService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IDposService dposService;

    @Autowired
    private IWitnessService witnessService;

    @Autowired
    private WitnessTimer witnessTimer;

    @Override
    public boolean isDposMiner(String address, String preBlockHash) {
        // get dpos miners address at branch which the preblock belonged to
        List<String> dposAddressList = dposService.getDposGroupByHeihgt(preBlockHash);
        return CollectionUtils.isNotEmpty(dposAddressList) && dposAddressList.contains(address);
    }

    @Override
    public boolean isMinerOnBest(String address) {
        ////check the miner own the MINER
        return transactionService.hasStake(address, SystemCurrencyEnum.MINER);
    }

    @Override
    public boolean isWitness(String address, long height) {
        //todo yuguojia 2018-7-22 judge by address and height
        return witnessService.isWitness(address);
    }

    @Override
    public boolean isGuarder(String address, String preBlockHash) {
        return transactionService.hasStake(preBlockHash, address, SystemCurrencyEnum.GUARDER);
    }

    @Override
    public boolean isGenesisBlock(Block block) {
        if (block == null) {
            return false;
        }
        if (block.getHeight() == 1 &&
                block.getPrevBlockHash() == null &&
                StringUtils.equals(config.getGenesisBlockHash(), block.getHash())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isGenesisBlock(String blockHash) {
        return StringUtils.equals(config.getGenesisBlockHash(), blockHash);
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
        result = witnessTimer.acceptBlock(block);
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