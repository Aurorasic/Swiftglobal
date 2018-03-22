package cn.primeledger.cas.global.api.service;

import cn.primeledger.cas.global.api.vo.MinerBlock;
import cn.primeledger.cas.global.blockchain.transaction.SystemCurrencyEnum;
import cn.primeledger.cas.global.blockchain.transaction.UTXO;
import cn.primeledger.cas.global.crypto.ECKey;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author kongyu
 * @date 2018-03-20
 */
@Slf4j
@Service
public class MinerRespService {
    @Resource(name = "utxoData")
    ConcurrentMap<String, UTXO> utxoData;

    /**
     * Count all the miners in the network
     * @return
     */
    public long statisticsMinerNumber() {
        long count = 0L;
        List<UTXO> utxoList = utxoData.values().stream().filter(utxo -> StringUtils.equals(SystemCurrencyEnum.MINER.getCurrency(), utxo.getOutput().getCurrency())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(utxoList)) {
            return count;
        }
        for (UTXO utxo : utxoList) {
            if (BigDecimal.ONE.compareTo(utxo.getOutput().getAmount()) <= 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Judge a pubKey is a miner or not
     * @param pubKey
     * @return
     */
    public Boolean checkIsMinerByPubKey(String pubKey) {
        Boolean isMienr = false;
        List<UTXO> utxoList = utxoData.values().stream().filter(utxo -> StringUtils.equals(SystemCurrencyEnum.MINER.getCurrency(), utxo.getOutput().getCurrency())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(utxoList)) {
            return isMienr;
        }

        String address = ECKey.fromPublicKeyOnly(pubKey).toBase58Address();
        if (null == address) {
            return isMienr;
        }

        for (UTXO utxo : utxoList) {
            if (StringUtils.equals(utxo.getOutput().getLockScript().getAddress(), address) &&
                    BigDecimal.ONE.compareTo(utxo.getOutput().getAmount()) <= 0) {
                isMienr = true;
                break;
            }
        }
        return isMienr;
    }

    /**
     * A list of all the block information unearthed by a miner
     * @param pubKey
     * @return
     */
    public List<MinerBlock> statisticsMinerBlocks(String pubKey) {
        if (null == pubKey) {
            return null;
        }
        return null;
    }
}
