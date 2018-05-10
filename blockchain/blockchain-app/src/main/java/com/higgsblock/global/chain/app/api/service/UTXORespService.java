package com.higgsblock.global.chain.app.api.service;

import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author kongyu
 * @date 2018-03-19
 */
@Slf4j
@Service
public class UTXORespService {
    @Resource(name = "myUTXOData")
    private ConcurrentMap<String, UTXO> myUTXOData;

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

        List<UTXO> list = myUTXOData.values().stream().collect(Collectors.toList());
        return list;
    }
}
