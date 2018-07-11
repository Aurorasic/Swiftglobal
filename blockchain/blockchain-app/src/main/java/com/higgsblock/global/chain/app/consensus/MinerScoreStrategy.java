package com.higgsblock.global.chain.app.consensus;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author yuguojia
 * @date 2018/03/02
 **/
@Component
@Slf4j
public class MinerScoreStrategy {
    public int INIT_SCORE = 1000;
    public int MINUS_SCORE_PACKAGED_BEST = -20;

    @Autowired
    private IScoreService scoreDaoService;
    @Autowired
    private TransactionService transactionService;

    /**
     * Called by block dao service
     */

    public void refreshMinersScore(Block toBeBestBlock) {

        BlockWitness minerPKSig = toBeBestBlock.getMinerFirstPKSig();

        //if the block is only mined by  miner, plus score
        if (transactionService.hasStake(minerPKSig.getAddress(), SystemCurrencyEnum.MINER)) {
            //minus miner score
            plusScore(minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_BEST);
            LOGGER.info("miner:{}, score plus:{}", minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_BEST);
        }

        //handle joined miner and removed miner
        LOGGER.info("begin to handle joined miner and removed miner,bestBlock={}", toBeBestBlock.getHash());
        List<Transaction> transactions = toBeBestBlock.getTransactions();
        for (Transaction tx : transactions) {
            LOGGER.info("calc removing and adding miner currency,tx={}", tx.getHash());
            Set<String> removedMiners = transactionService.getRemovedMiners(tx);
            for (String removedMiner : removedMiners) {
                scoreDaoService.remove(removedMiner);
            }

            Set<String> addedMiners = transactionService.getAddedMiners(tx);
            for (String addedMiner : addedMiners) {
                scoreDaoService.putIfAbsent(addedMiner, INIT_SCORE);
            }
        }
        LOGGER.info("end to handle joined miner and removed miner,bestBlock={}", toBeBestBlock.getHash());

    }

    private void plusScore(String address, int plusScore) {
        if (StringUtils.isNotEmpty(address) && plusScore != 0) {
            Integer tmpScore = scoreDaoService.get(address);
            int score = tmpScore == null ? INIT_SCORE : tmpScore;
            score += plusScore;
            scoreDaoService.put(address, score);
        }// else it is empty address, do not handle
    }
}