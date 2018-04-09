package cn.primeledger.cas.global.api.service;

import cn.primeledger.cas.global.api.vo.MinerBlock;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.transaction.SystemCurrencyEnum;
import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.blockchain.transaction.TransactionType;
import cn.primeledger.cas.global.blockchain.transaction.UTXO;
import cn.primeledger.cas.global.crypto.ECKey;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.mapdb.BTreeMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.stream.Collectors;

/**
 * @author kongyu
 * @date 2018-03-20
 */
@Slf4j
@Service
public class MinerRespService {

    private final static long PRE_BLOCKS = 13;

    @Resource(name = "utxoData")
    ConcurrentMap<String, UTXO> utxoData;

    @Resource(name = "blockData")
    private ConcurrentMap<String, Block> blockData;

    @Resource(name = "pubKeyMap")
    private BTreeMap<byte[], byte[]> pubKeyMap;

    /**
     * Count all the miners in the network
     *
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
     *
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
     *
     * @param pubKey
     * @return
     */
    public List<MinerBlock> statisticsMineBlocks(String pubKey) {
        if (null == pubKey) {
            return null;
        }

        String address = ECKey.pubKey2Base58Address(pubKey);
        if (null == address) {
            throw new RuntimeException("address is null");
        }

        ConcurrentNavigableMap<byte[], byte[]> resultMap = pubKeyMap.prefixSubMap(address.getBytes(), true);
        if (resultMap.isEmpty()) {
            return null;
        }

        List<MinerBlock> minerBlocks = Lists.newArrayList();
        //Traverse the query to the map data.
        for (byte[] addrKey : resultMap.keySet()) {
            if (null == addrKey) {
                continue;
            }

            String[] addressAndTxHash = new String(addrKey).split(":");
            if (3 != addressAndTxHash.length) {
                continue;
            }

            if (!StringUtils.equals(TransactionType.COINBASE.getCode(), addressAndTxHash[2])) {
                continue;
            }

            String blockHash = new String(resultMap.get(addrKey));
            if (null == blockHash) {
                continue;
            }

            //Miner's reward block
            Block block = blockData.get(blockHash);
            //todo kongyu 2018-3-26 如果是创世块，无需统计 该块的收益
            if (null == block || 1 == block.getHeight()) {
                continue;
            }

            Block preBlock = null;
            //The miners dug out of the block.
            if (2 < block.getHeight()) {
                preBlock = blockData.get(block.getPrevBlockHash());
                if (null == preBlock) {
                    continue;
                }
            }

            List<Transaction> transactions = block.getTransactions();
            if (CollectionUtils.isEmpty(transactions)) {
                continue;
            }

            for (Transaction tx : transactions) {
                if (null == tx) {
                    continue;
                }
                if (StringUtils.equals(tx.getHash(), addressAndTxHash[1])) {
                    if (null != preBlock) {
                        MinerBlock minerBlock = MinerBlock.builder()
                                .height(preBlock.getHeight())
                                .blockTime(preBlock.getBlockTime())
                                .blockSize(preBlock.toString().getBytes().length)
                                .hash(preBlock.getHash())
                                .earnings(tx.getOutputs().get(0).getAmount())
                                .currency(tx.getOutputs().get(0).getCurrency())
                                .build();
                        minerBlocks.add(minerBlock);
                    } else {
                        MinerBlock minerBlock = MinerBlock.builder()
                                .height(block.getHeight())
                                .blockTime(block.getBlockTime())
                                .blockSize(block.toString().getBytes().length)
                                .hash(block.getHash())
                                .earnings(tx.getOutputs().get(0).getAmount())
                                .currency(tx.getOutputs().get(0).getCurrency())
                                .build();
                        minerBlocks.add(minerBlock);
                    }
                    break;
                }
            }
        }
        return minerBlocks;
    }
}
