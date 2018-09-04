package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.SignaturePair;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.common.ScoreRangeEnum;
import com.higgsblock.global.chain.app.dao.IScoreRepository;
import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import com.higgsblock.global.chain.app.service.IDposService;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.app.service.ITransactionService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private IScoreRepository scoreRepository;
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
        ScoreEntity scoreEntity = scoreRepository.findByAddress(address);
        return null == scoreEntity ? null : scoreEntity.getScore();
    }

    /**
     * set score
     *
     * @param address
     * @param score
     */
    @Override
    public void put(String address, Integer score) {
        ScoreEntity scoreEntity = scoreRepository.findByAddress(address);
        if (null != scoreEntity) {
            scoreEntity.setScore(score);
            scoreRepository.save(scoreEntity);
        } else {
            ScoreEntity saveEntity = new ScoreEntity(address, score);
            scoreRepository.save(saveEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBatch(List<String> addressList, int score) {
        return scoreRepository.updateByAddress(addressList, score);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int plusAll(Integer score) {
        return scoreRepository.plusAll(score);
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
        ScoreEntity scoreEntity = scoreRepository.findByAddress(address);
        if (scoreEntity == null) {
            scoreRepository.save(new ScoreEntity(address, score));
        }
    }

    /**
     * remove score
     *
     * @param address
     */
    @Override
    public void remove(String address) {
        scoreRepository.deleteByAddress(address);
    }

    /**
     * query all score
     *
     * @return
     */
    @Override
    public Map<String, Integer> loadAll() {
        Map<String, Integer> map = Maps.newHashMap();
        scoreRepository.findAll().forEach(e -> map.put(e.getAddress(), e.getScore()));
        return map;
    }

    @Override
    public List<ScoreEntity> all() {
        return scoreRepository.findAll();
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
        Pageable pageable = new PageRequest(0, SCORE_LIMIT_NUM, Sort.Direction.DESC, SCORE_ORDERBY_FIELD, ADDRESS_ORDERBY_FIELD);
        List<String> placeList = new ArrayList<>();
        placeList.add("");
        List<ScoreEntity> scores = scoreRepository.queryTopScoreByRange(
                scoreRange.getMinScore(),
                scoreRange.getMaxScore(),
                CollectionUtils.isEmpty(exculdeAddresses) ? placeList : exculdeAddresses,
                pageable);
        if (scores == null) {
            return Lists.newLinkedList();
        }
        List<String> addresses = Lists.newLinkedList();
        scores.forEach(scoreEntity -> addresses.add(scoreEntity.getAddress()));
        return addresses;
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
