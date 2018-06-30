package com.higgsblock.global.chain.app.consensus;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import com.higgsblock.global.chain.app.service.IScoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public static void refreshMinersScore(Block bestBlock) {

        BlockWitness minerPKSig = bestBlock.getMinerFirstPKSig();
        Map<String, Integer> allMinerSoreMap = scoreDaoService.loadAll();

        //minus miner score
        plusScore(allMinerSoreMap, minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_BEST);

        //handle joined miner and removed miner
        List<Transaction> transactions = bestBlock.getTransactions();
        for (Transaction tx : transactions) {
            Set<String> removedMiners = transactionService.getRemovedMiners(tx);
            for (String removedMiner : removedMiners) {
                scoreDaoService.remove(removedMiner);
            }

            Set<String> addedMiners = transactionService.getAddedMiners(tx);
            for (String addedMiner : addedMiners) {
                scoreDaoService.putIfAbsent(addedMiner, INIT_SCORE);
            }
        }
        LOGGER.info("handled joined miner and removed miner,bestBlock={}", bestBlock.getHash());

    }

    private static void plusScore(Map<String, Integer> allMinerSoreMap, String address, int plusScore) {
        if (StringUtils.isNotEmpty(address) && plusScore != 0) {
            Integer tmpScore = scoreDaoService.get(address);
            int score = tmpScore == null ? INIT_SCORE : tmpScore;
            score += plusScore;
            scoreDaoService.put(address, score);
        }// else it is empty address, do not handle
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