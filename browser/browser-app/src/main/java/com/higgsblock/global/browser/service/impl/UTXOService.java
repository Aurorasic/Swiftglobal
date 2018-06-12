package com.higgsblock.global.browser.service.impl;

import com.higgsblock.global.browser.dao.entity.UTXOPO;
import com.higgsblock.global.browser.dao.iface.IUTXODAO;
import com.higgsblock.global.browser.service.iface.IUTXOService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Statement;
import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-25
 */
@Slf4j
@Service
public class UTXOService implements IUTXOService {

    @Autowired
    private IUTXODAO iutxoDao;

    @Override
    public void batchInsert(List<UTXOPO> utxoPos) {
        if (CollectionUtils.isEmpty(utxoPos)) {
            LOGGER.warn("utxo list is empty ");
            return;
        }
        int[] ints = iutxoDao.batchInsert(utxoPos);
        for (int anInt : ints) {
            if (anInt < 0 && anInt != Statement.SUCCESS_NO_INFO) {
                LOGGER.error("utxo batchInsert result error");
                throw new RuntimeException("utxo batchInsert error");
            }
        }
        LOGGER.info("utxo batchInsert success");
    }

    @Override
    public boolean deleteUTXO(String transactionHash, short index) {
        if (StringUtils.isEmpty(transactionHash) || index < 0) {
            LOGGER.error("transactionHash is empty or index < 0");
            return false;
        }
        int num = iutxoDao.deleteUTXO(transactionHash, index);
        if (num == 1) {
            LOGGER.info("delete utxo success   txHash ={} index = {}", transactionHash, index);
            return true;
        }
        return false;
    }

    @Override
    public boolean batchDeleteUTXO(List<String> txHashIndexs) {
        boolean result = false;
        String[] temp = null;
        String txHash = null;
        short index = -1;
        if (CollectionUtils.isNotEmpty(txHashIndexs)) {
            for (String txHashIndex : txHashIndexs) {
                temp = txHashIndex.split("_");
                txHash = temp[0];
                index = Short.valueOf(temp[1]);
                result = deleteUTXO(txHash, index);
                if (!result) {
                    LOGGER.error("delete utxo failure by txHash ={} index = {}", txHash, index);
                    throw new RuntimeException("delete utxo failure");
                }
            }
        }
        return result;
    }

    @Override
    public List<UTXOPO> getUTXOsByAddress(String address) {
        if (StringUtils.isEmpty(address)) {
            LOGGER.error("address is empty");
            return null;
        }

        List<UTXOPO> utxoPos = iutxoDao.getByField(address);
        if (CollectionUtils.isNotEmpty(utxoPos)) {
            return utxoPos;
        }

        LOGGER.info("The address has not utxo  address = {}", address);
        return null;
    }
}
