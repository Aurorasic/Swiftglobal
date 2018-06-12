package com.higgsblock.global.browser.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.browser.dao.entity.TransactionInputPO;
import com.higgsblock.global.browser.dao.entity.TransactionOutputPO;
import com.higgsblock.global.browser.dao.entity.TransactionPO;
import com.higgsblock.global.browser.dao.iface.ITransactionDAO;
import com.higgsblock.global.browser.service.bo.*;
import com.higgsblock.global.browser.service.iface.ITransactionInputService;
import com.higgsblock.global.browser.service.iface.ITransactionOutputService;
import com.higgsblock.global.browser.service.iface.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Service
@Slf4j
public class TransactionService implements ITransactionService {

    @Autowired
    private ITransactionDAO iTransactionDao;

    @Autowired
    private ITransactionInputService iTransacionInputService;

    @Autowired
    private ITransactionOutputService iTransactionOutputService;

    @Override
    public TransactionBO getTransactionByHash(String hash) {
        if (StringUtils.isEmpty(hash)) {
            LOGGER.error("transaction hash is empty ");
            return null;
        }
        TransactionBO transactionBo = null;
        try {
            List<TransactionPO> transactionPos = iTransactionDao.getByField(hash);
            if (CollectionUtils.isEmpty(transactionPos)) {
                return null;
            }
            //only get coinBase TransactionPO
            TransactionPO transactionPo = transactionPos.get(0);
            transactionBo = new TransactionBO();
            transactionBo.setTxHash(transactionPo.getTransactionHash());
            transactionBo.setVersion(transactionPo.getVersion());
            transactionBo.setExtra(transactionPo.getExtra());
            transactionBo.setLockTime(transactionPo.getLockTime());
            PubKeyAndSignPairBO pubKeyAndSignPairBo = new PubKeyAndSignPairBO();

            List<TransactionInputBO> txInputsBo = Lists.newArrayList();
            List<TransactionInputPO> txInputs = iTransacionInputService.getByField(hash);
            if (CollectionUtils.isNotEmpty(txInputs)) {
                List<String> addressList = Arrays.asList(txInputs.get(0).getAddressList().split(":"));
                pubKeyAndSignPairBo.setPubKey(addressList.get(0));
                txInputs.forEach(transactionInput -> {
                    TransactionInputBO txInputBo = new TransactionInputBO();
                    TransactionOutPointBO txOutPoint = new TransactionOutPointBO();
                    txOutPoint.setHash(transactionInput.getPreTransactionHash());
                    txOutPoint.setIndex(transactionInput.getPreOutIndex());
                    txInputBo.setPreOut(txOutPoint);
                    txInputBo.setUnLockScript(addressList);
                    txInputsBo.add(txInputBo);
                });
            }

            List<TransactionOutPutBO> txOutputsBo = Lists.newArrayList();
            List<TransactionOutputPO> txOutputs = iTransactionOutputService.getTransactionOutPuts(hash);
            if (CollectionUtils.isNotEmpty(txOutputs)) {
                txOutputs.forEach(transactionOutput -> {
                    TransactionOutPutBO outPutBo = new TransactionOutPutBO();
                    LockScriptBO lockScriptBo = new LockScriptBO();
                    lockScriptBo.setAddress(transactionOutput.getAddress());
                    lockScriptBo.setTpye(transactionOutput.getScriptType());

                    outPutBo.setAmount(transactionOutput.getAmount());
                    outPutBo.setCurrency(transactionOutput.getCurrency());
                    outPutBo.setLockScript(lockScriptBo);
                    txOutputsBo.add(outPutBo);
                });
            }

            transactionBo.setInputs(txInputsBo);
            transactionBo.setOutputs(txOutputsBo);
            transactionBo.setPubKeyAndSignPair(pubKeyAndSignPairBo);
        } catch (Exception e) {
            return null;
        }
        return transactionBo;
    }

    @Override
    public List<String> getTxHashByBlockHash(String blockHash) {
        if (StringUtils.isEmpty(blockHash)) {
            LOGGER.error("block hash is empty");
            return null;
        }
        return iTransactionDao.getTxHashByBlockHash(blockHash);
    }

    @Override
    public TransactionItemsBO getTransactionByPk(String pubKey) {
        TransactionItemsBO txItemsBoByInputs = iTransacionInputService.getTxInputBosByPubKey(pubKey);
        if (txItemsBoByInputs == null) {
            return null;
        }
        TransactionItemsBO txItemsBoByOutputs = iTransactionOutputService.getTxOutputBosByPubKey(pubKey);
        if (txItemsBoByOutputs == null) {
            return null;
        }
        Set<TransactionBO> transactionBos = new HashSet<>();
        //remove duplication transactionBo
        transactionBos.addAll(txItemsBoByInputs.getItems());
        transactionBos.addAll(txItemsBoByOutputs.getItems());

        List<TransactionBO> transactionBoList = Lists.newArrayList();
        transactionBoList.addAll(transactionBos);
        TransactionItemsBO boItems = new TransactionItemsBO();
        boItems.setItems(transactionBoList);
        return boItems;
    }

    @Override
    public void batchInsert(List<TransactionPO> transactionPos) {
        if (CollectionUtils.isEmpty(transactionPos)) {
            LOGGER.warn("transaction list empty");
            return;
        }
        int[] ints = iTransactionDao.batchInsert(transactionPos);
        for (int anInt : ints) {
            if (anInt < 0 && anInt != Statement.SUCCESS_NO_INFO) {
                LOGGER.error("transaction batchInsert result error");
                throw new RuntimeException("transaction batchInsert error");
            }
        }
        LOGGER.info("transaction batchInsert success");
    }
}
