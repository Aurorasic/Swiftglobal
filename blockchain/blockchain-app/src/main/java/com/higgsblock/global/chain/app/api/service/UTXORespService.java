package com.higgsblock.global.chain.app.api.service;

import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.UtxoDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kongyu
 * @date 2018-03-19
 */
@Slf4j
@Service
public class UTXORespService {

    @Autowired
    private UtxoDao utxoDao;

    /**
     * Query the corresponding UTXOS according to the address information
     *
     * @param addr
     * @return
     */
    public List<UTXO> getUTXOsByAddress(String addr) {
        if (null == addr) {
            throw new RuntimeException("addr is null");
        }
        return utxoDao.allValues().stream().filter(utxo -> utxo.getAddress().equals(addr)).collect(Collectors.toList());
    }
}
