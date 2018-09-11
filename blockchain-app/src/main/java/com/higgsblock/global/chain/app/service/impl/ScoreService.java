package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.SignaturePair;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.common.ScoreRangeEnum;
import com.higgsblock.global.chain.app.keyvalue.annotation.Transactional;
import com.higgsblock.global.chain.app.service.IBlockChainInfoService;
import com.higgsblock.global.chain.app.service.IDposService;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.app.service.ITransactionService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author HuangShengli
 * @date 2018-05-23
 */
@Slf4j
@Service
public class ScoreService implements IScoreService {

    @Autowired
    private IBlockChainInfoService blockChainInfoService;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IDposService dposService;

    /**
     * get score by address
     *
     * @param address
     * @return
     */
    @Override
    public Integer get(String address) {
        Map<String, String> allScores = blockChainInfoService.getAllScores();
        String score = allScores.get(address);
        return score == null ? null : Integer.valueOf(score);
    }

    /**
     * set score
     *
     * @param address
     * @param score
     */
    @Override
    public void put(String address, Integer score) {
        Map<String, String> allScores = blockChainInfoService.getAllScores();
        allScores.put(address, String.valueOf(score));
        blockChainInfoService.setAllScores(allScores);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBatch(List<String> addressList, int score) {
        for (String address : addressList) {
            put(address, score);
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int plusAll(Integer score) {
        Map<String, String> allScores = blockChainInfoService.getAllScores();
        if (allScores.isEmpty()) {
            return 1;
        }
        Set<String> keySet = allScores.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String oldScoreStr = allScores.get(key);
            Integer oldScore = Integer.valueOf(oldScoreStr);
            allScores.put(key, String.valueOf(oldScore + score));
        }

        blockChainInfoService.setAllScores(allScores);
        return 1;
    }

    /**
     * set score if not exist
     *
     * @param address
     * @param score
     * @return
     */
    @Override
    public void putIfAbsent(String address, Integer score) {
        Map<String, String> allScores = blockChainInfoService.getAllScores();
        String value = allScores.get(address);
        if (value == null) {
            allScores.put(address, String.valueOf(score));
            blockChainInfoService.setAllScores(allScores);
        }
    }

    /**
     * remove score
     *
     * @param address
     */
    @Override
    public void remove(String address) {
        Map<String, String> allScores = blockChainInfoService.getAllScores();
        String value = allScores.get(address);
        if (value != null) {
            allScores.remove(address);
            blockChainInfoService.setAllScores(allScores);
        }
    }

    /**
     * set score for lucky miners
     *
     * @param addressList
     */
    @Override
    public void setSelectedDposScore(List<String> addressList) {
        if (CollectionUtils.isEmpty(addressList)) {
            return;
        }
        updateBatch(addressList, SELECTED_DPOS_SET_SCORE);
    }

    /**
     * refresh score for miner who produced the best block
     *
     * @param toBeBestBlock
     */
    @Override
    public void refreshMinersScore(Block toBeBestBlock) {
        updateScores(toBeBestBlock);

        //handle joined miner and removed miner
        List<Transaction> transactions = toBeBestBlock.getTransactions();
        for (Transaction tx : transactions) {
            LOGGER.info("calc removing and adding miner currency,tx={}", tx.getHash());
            Set<String> removedMiners = transactionService.getRemovedMiners(tx);
            for (String removedMiner : removedMiners) {
                remove(removedMiner);
            }

            Set<String> addedMiners = transactionService.getAddedMiners(tx);
            for (String addedMiner : addedMiners) {
                putIfAbsent(addedMiner, INIT_SCORE);
            }
        }
        LOGGER.info("end to handle joined miner and removed miner,bestBlock={}", toBeBestBlock.getHash());
    }

    /**
     * list top 1000 by score range
     *
     * @param scoreRange
     * @param exculdeAddresses
     * @return
     */
    @Override
    public List<String> queryAddresses(ScoreRangeEnum scoreRange, List<String> exculdeAddresses) {
        List<String> result = Lists.newLinkedList();
        //todo yuguojia find all, then sort
        Map<String, String> allScores = blockChainInfoService.getAllScores();
        Set<Map.Entry<String, String>> entries = allScores.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String key = entry.getKey();
            String value = entry.getValue();
            Integer score = Integer.valueOf(value);
            if (score < scoreRange.getMinScore() ||
                    score >= scoreRange.getMaxScore()) {
                //only in [minScore,maxScore)
                continue;
            }
            if (result.size() == SCORE_LIMIT_NUM) {
                break;
            }
            if (!exculdeAddresses.contains(key)) {
                result.add(key);
            }
        }
        return result;
    }

    private void updateScores(Block toBeBestBlock) {
        SignaturePair minerPKSig = toBeBestBlock.getMinerSigPair();
        //if the block is only mined by  miner, set score
        if (transactionService.hasStake(minerPKSig.getAddress(), SystemCurrencyEnum.MINER)) {
            put(minerPKSig.getAddress(), MINED_BLOCK_SET_SCORE);
        } else {
            //mined by backup peer node
            String prevBlockHash = toBeBestBlock.getPrevBlockHash();
            List<String> dposAddressList = dposService.getRestDposMinersByPreHash(prevBlockHash);
            if (CollectionUtils.isNotEmpty(dposAddressList)) {
                updateBatch(dposAddressList, OFFLINE_MINER_SET_SCORE);
            }
        }
        plusAll(ONE_BLOCK_ADD_SCORE);
    }
}
