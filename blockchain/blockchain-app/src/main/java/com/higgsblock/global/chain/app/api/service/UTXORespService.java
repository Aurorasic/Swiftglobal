package com.higgsblock.global.chain.app.api.service;

import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.service.UTXODaoServiceProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author kongyu
 * @date 2018-03-19
 */
@Slf4j
@Service
public class UTXORespService {

    @Autowired
    private UTXODaoServiceProxy utxoDaoServiceProxy;

    /**
     * Query the corresponding UTXOS according to the address information
     *
     * @param addr
     * @return
     */
    public List<UTXO> getUTXOsByAddress(String addr) {
        return utxoDaoServiceProxy.getUnionUTXO(addr);
    }
}
