package com.higgsblock.global.browser.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.browser.dao.entity.TransactionOutputPO;
import com.higgsblock.global.browser.dao.iface.ITransactionOutputDAO;
import com.higgsblock.global.browser.service.bo.TransactionBO;
import com.higgsblock.global.browser.service.bo.TransactionItemsBO;
import com.higgsblock.global.browser.service.iface.ITransactionOutputService;
import com.higgsblock.global.browser.service.iface.ITransactionService;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Su Jiulong
 * @date 2018-05-24
 */
@Service
@Slf4j
public class TransactionOutputService implements ITransactionOutputService {

    @Autowired
    private ITransactionOutputDAO transactionOutputDao;

    @Autowired
    private ITransactionService iTransactionService;

    @Override
    public TransactionOutputPO getTxOutput(String transactionHash, short index) {
        if (StringUtils.isEmpty(transactionHash) || index < 0) {
            LOGGER.error("previous transactionHash is empty or index < 0  txHash = {}, index = {}", transactionHash, index);
            throw new RuntimeException("previous transactionHash is empty or index < 0");
        }
        List<TransactionOutputPO> transactionOutputs = transactionOutputDao.getTxOutput(transactionHash, index);
        if (CollectionUtils.isEmpty(transactionOutputs)) {
            LOGGER.error("not find TransactionOutputPO by the transactionHash and index. " +
                    "txHash = {}, index = {}", transactionHash, index);
            throw new RuntimeException("not find TransactionOutputPO by the transactionHash and index");
        }
        return transactionOutputs.get(0);
    }

    @Override
    public List<TransactionOutputPO> getTransactionOutPuts(String hash) {
        if (StringUtils.isEmpty(hash)) {
            LOGGER.error("transaction hash is empty");
            throw new RuntimeException("txHash is empty");
        }
        return transactionOutputDao.getByField(hash);
    }

    @Override
    public TransactionItemsBO getTxOutputBosByPubKey(String pubKey) {
        if (StringUtils.isEmpty(pubKey)) {
            return null;
        }
        String address = null;
        try {
            address = ECKey.pubKey2Base58Address(pubKey);
        } catch (Exception e) {
            LOGGER.error("pubKey2Base58Address error pubKey = {}", pubKey);
            throw new RuntimeException("pubKey2Base58Address error pubKey = " + pubKey);
        }
        List<String> transactionHashs = transactionOutputDao.getTxHashsByAddress(address);
        Set<String> txHashs = new HashSet<>(transactionHashs);
        if (CollectionUtils.isEmpty(txHashs)) {
            return null;
        }
        List<TransactionBO> transactionBos = Lists.newArrayList();
        txHashs.forEach(transactionHash -> {
            TransactionBO transactionBo = iTransactionService.getTransactionByHash(transactionHash);
            transactionBos.add(transactionBo);
        });
        TransactionItemsBO transactionItemsBo = new TransactionItemsBO();
        transactionItemsBo.setItems(transactionBos);
        return transactionItemsBo;
    }

    @Override
    public void batchInsert(List<TransactionOutputPO> transactionOutputs) {
        if (CollectionUtils.isEmpty(transactionOutputs)) {
            LOGGER.warn("transactionOutput list is empty ");
            return;
        }
        int[] ints = transactionOutputDao.batchInsert(transactionOutputs);
        for (int anInt : ints) {
            if (anInt < 0 && anInt != Statement.SUCCESS_NO_INFO) {
                LOGGER.error("transactionOutput batchInsert result error");
                throw new RuntimeException("transactionOutput batchInsert result error");
            }
        }
        LOGGER.info("transactionOutput batchInsert success");
    }
}
