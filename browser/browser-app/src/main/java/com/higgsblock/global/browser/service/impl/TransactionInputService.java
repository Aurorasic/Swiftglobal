package com.higgsblock.global.browser.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.browser.dao.entity.TransactionInputPO;
import com.higgsblock.global.browser.dao.iface.ITransactionInputDAO;
import com.higgsblock.global.browser.service.bo.TransactionBO;
import com.higgsblock.global.browser.service.bo.TransactionItemsBO;
import com.higgsblock.global.browser.service.iface.ITransactionInputService;
import com.higgsblock.global.browser.service.iface.ITransactionService;
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
 * @date 2018-05-25
 */
@Service
@Slf4j
public class TransactionInputService implements ITransactionInputService {

    @Autowired
    private ITransactionInputDAO transactionInputDao;

    @Autowired
    private ITransactionService iTransactionService;

    @Override
    public void batchInsert(List<TransactionInputPO> transactionInputPos) {
        if (CollectionUtils.isEmpty(transactionInputPos)) {
            LOGGER.error("transactionInput list is empty");
            return;
        }
        int[] ints = transactionInputDao.batchInsert(transactionInputPos);
        for (int anInt : ints) {
            if (anInt < 0 && anInt != Statement.SUCCESS_NO_INFO) {
                LOGGER.error("transactionInput batchInsert result error");
                throw new RuntimeException("transactionInput batchInsert result error");
            }
        }
        LOGGER.info("transactionInput batchInsert success");
    }

    @Override
    public TransactionItemsBO getTxInputBosByPubKey(String pubKey) {
        if (StringUtils.isEmpty(pubKey)) {
            return null;
        }
        List<String> transactionHashs = transactionInputDao.getTxHashsByPubKey(pubKey);

        if (CollectionUtils.isEmpty(transactionHashs)) {
            return null;
        }
        //remove duplication
        Set<String> txHashs = new HashSet<>(transactionHashs);

        List<TransactionBO> transactionBos = Lists.newArrayList();
        txHashs.forEach(transactionHash -> {
            //get transactionBo by txHash
            TransactionBO transactionBo = iTransactionService.getTransactionByHash(transactionHash);
            transactionBos.add(transactionBo);
        });
        TransactionItemsBO transactionItemsBo = null;
        if (CollectionUtils.isNotEmpty(transactionBos)) {
            transactionItemsBo = new TransactionItemsBO();
            transactionItemsBo.setItems(transactionBos);
        }
        return transactionItemsBo;
    }

    @Override
    public List<TransactionInputPO> getByField(String hash) {
        if (StringUtils.isEmpty(hash)) {
            return null;
        }
        return transactionInputDao.getByField(hash);
    }
}
