package com.higgsblock.global.chain.app.blockchain.consensus;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionProcessor;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
    public static int INIT_SCORE = 600;

    //old score strategy
    public static int MINUS_SCORE_PACKAGED_BEST = -20;

    //new score strategy
    public static int SELECTED_DPOS_SET_SCORE = 600;
    public static int MINED_BLOCK_SET_SCORE = 800;
    public static int OFFLINE_MINER_SET_SCORE = 0;
    public static int ONE_BLOCK_ADD_SCORE = 1;

    @Autowired
    private IScoreService scoreDaoService;
    @Autowired
    private TransactionProcessor transactionProcessor;
    @Autowired
    private NodeProcessor nodeProcessor;

    public void setSelectedDposScore(List<String> addressList) {
        if (CollectionUtils.isEmpty(addressList)) {
            return;
        }
        scoreDaoService.updateBatch(addressList, SELECTED_DPOS_SET_SCORE);
    }

    /**
     * Called by block dao service
     */
    public void refreshMinersScore(Block toBeBestBlock) {

        newScoreStrategy(toBeBestBlock);

        //handle joined miner and removed miner
        LOGGER.info("begin to handle joined miner and removed miner,bestBlock={}", toBeBestBlock.getHash());
        List<Transaction> transactions = toBeBestBlock.getTransactions();
        for (Transaction tx : transactions) {
            LOGGER.info("calc removing and adding miner currency,tx={}", tx.getHash());
            Set<String> removedMiners = transactionProcessor.getRemovedMiners(tx);
            for (String removedMiner : removedMiners) {
                scoreDaoService.remove(removedMiner);
            }

            Set<String> addedMiners = transactionProcessor.getAddedMiners(tx);
            for (String addedMiner : addedMiners) {
                scoreDaoService.putIfAbsent(addedMiner, INIT_SCORE);
            }
        }
        LOGGER.info("end to handle joined miner and removed miner,bestBlock={}", toBeBestBlock.getHash());
    }

    private void oldScoreStrategy(Block toBeBestBlock) {
        BlockWitness minerPKSig = toBeBestBlock.getMinerFirstPKSig();
        //if the block is only mined by  miner
        if (transactionProcessor.hasStake(minerPKSig.getAddress(), SystemCurrencyEnum.MINER)) {
            //set miner score to 600
            plusScore(minerPKSig.getAddress(), MINUS_SCORE_PACKAGED_BEST);
        }
    }

    private void newScoreStrategy(Block toBeBestBlock) {
        BlockWitness minerPKSig = toBeBestBlock.getMinerFirstPKSig();
        //if the block is only mined by  miner, set score
        if (transactionProcessor.hasStake(minerPKSig.getAddress(), SystemCurrencyEnum.MINER)) {
            setScore(minerPKSig.getAddress(), MINED_BLOCK_SET_SCORE);
        } else {
            //mined by backup peer node
            long blockHeight = toBeBestBlock.getHeight();
            String prevBlockHash = toBeBestBlock.getPrevBlockHash();
            List<String> dposAddressList = nodeProcessor.getDposGroupByHeihgt(blockHeight, prevBlockHash);
            scoreDaoService.updateBatch(dposAddressList, OFFLINE_MINER_SET_SCORE);
        }
        scoreDaoService.plusAll(ONE_BLOCK_ADD_SCORE);
    }

    private void plusScore(String address, int plusScore) {
        if (StringUtils.isNotEmpty(address) && plusScore != 0) {
            Integer tmpScore = scoreDaoService.get(address);
            int score = tmpScore == null ? INIT_SCORE : tmpScore;
            score += plusScore;
            scoreDaoService.put(address, score);
        }// else it is empty address, do not handle
    }

    private void setScore(String address, int score) {
        if (StringUtils.isNotEmpty(address)) {
            scoreDaoService.put(address, score);
        }
    }
}