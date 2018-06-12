package com.higgsblock.global.chain.app.consensus;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.service.IScoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuguojia
 * @date 2018/03/02
 **/
@Component
@Slf4j
public class MinerScoreStrategy {
    public static int INIT_SCORE = 1000;

    public static int MINUS_SCORE_PACKAGED_BEST = -20;

    private static IScoreService scoreDaoService;
    private static BlockService blockService;
    private static TransactionService transactionService;
    private static NodeManager nodeManager;


    /**
     * Called by block dao service
     */
    public static List<BaseDaoEntity> refreshMinersScore(Block bestBlock) throws RocksDBException {
        List<BaseDaoEntity> entityList = new ArrayList<>();

        BlockWitness minerPKSig = bestBlock.getMinerFirstPKSig();
        List<BlockWitness> otherPKSigs = bestBlock.getOtherWitnessSigPKS();

        Map<String, Integer> allMinerSoreMap = scoreDaoService.loadAll();

        //minus miner score
        BaseDaoEntity plusEntity = plusScore(allMinerSoreMap, minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_BEST);
        entityList.add(plusEntity);

        //handle joined miner and removed miner
        List<Transaction> transactions = bestBlock.getTransactions();
        for (Transaction tx : transactions) {
            Set<String> removedMiners = transactionService.getRemovedMiners(tx);
            for (String removedMiner : removedMiners) {
                BaseDaoEntity removeEntity = scoreDaoService.remove(removedMiner);
                entityList.add(removeEntity);
            }

            Set<String> addedMiners = transactionService.getAddedMiners(tx);
            for (String addedMiner : addedMiners) {
                BaseDaoEntity putEntity = scoreDaoService.putIfAbsent(addedMiner, INIT_SCORE);
                entityList.add(putEntity);
            }
        }
        LOGGER.info("handled joined miner and removed miner,bestBlock={}", bestBlock.getHash());

        return entityList;
    }

    private static BaseDaoEntity plusScore(Map<String, Integer> allMinerSoreMap, String address, int plusScore) throws RocksDBException {
        if (StringUtils.isNotEmpty(address) && plusScore != 0) {
            Integer tmpScore = scoreDaoService.get(address);
            int score = tmpScore == null ? INIT_SCORE : tmpScore;
            score += plusScore;
            BaseDaoEntity entity = scoreDaoService.put(address, score);

            return entity;
        }// else it is empty address, do not handle

        return null;
    }


    @Autowired(required = true)
    public void setScoreDaoService(IScoreService scoreDaoService) {
        MinerScoreStrategy.scoreDaoService = scoreDaoService;
    }

    @Autowired(required = true)
    public void setBlockService(BlockService blockService) {
        MinerScoreStrategy.blockService = blockService;
    }

    @Autowired(required = true)
    public void setTransactionService(TransactionService transactionService) {
        MinerScoreStrategy.transactionService = transactionService;
    }

    @Autowired(required = true)
    public void setBlockService(NodeManager nodeManager) {
        MinerScoreStrategy.nodeManager = nodeManager;
    }

}