package cn.primeledger.cas.global.api.service;

import cn.primeledger.cas.global.blockchain.transaction.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
    @Resource(name = "utxoData")
    private ConcurrentMap<String, UTXO> utxoMap;

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

        List<UTXO> list = utxoMap.values().stream().filter(utxo -> StringUtils.equals(addr, utxo.getAddress())).collect(Collectors.toList());
        return list;
    }
}
