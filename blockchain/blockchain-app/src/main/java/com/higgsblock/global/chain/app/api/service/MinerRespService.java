package com.higgsblock.global.chain.app.api.service;

import com.higgsblock.global.chain.app.api.vo.MinerBlock;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.mapdb.BTreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;

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

    @Autowired
    private TransactionService transactionService;

    /**
     * Count all the miners in the network
     *
     * @return
     */
    public long statisticsMinerNumber() {
        return transactionService.getMinerNumber();
    }

    /**
     * Judge a pubKey is a miner or not
     *
     * @param pubKey
     * @return
     */
    public Boolean checkIsMinerByPubKey(String pubKey) {
        return transactionService.hasMinerStake(ECKey.pubKey2Base58Address(pubKey));
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
        Set<byte[]> addrKeys = resultMap.keySet();
        if (CollectionUtils.isEmpty(addrKeys)) {
            return null;
        }
        for (byte[] addrKey : addrKeys) {
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
            if (null == block || 1 == block.getHeight()) {
                continue;
            }

            Block preBlock = null;
            //The miners dug out of the block.
            if (2 <= block.getHeight()) {
                preBlock = blockData.get(block.getPrevBlockHash());
                if (null == preBlock) {
                    throw new RuntimeException("Blocks with a height greater than or equal to 2 do not have a front block.");
                }
            }

            List<Transaction> transactions = block.getTransactions();
            if (CollectionUtils.isEmpty(transactions)) {
                throw new RuntimeException("The transaction in the block cannot be empty.");
            }

            for (Transaction tx : transactions) {
                if (null == tx) {
                    throw new RuntimeException("Tx cannot be empty.");
                }
                if (StringUtils.equals(tx.getHash(), addressAndTxHash[1])) {
                    if (null != preBlock) {
                        MinerBlock minerBlock = MinerBlock.builder()
                                .height(preBlock.getHeight())
                                .blockTime(preBlock.getBlockTime())
                                .blockSize(preBlock.toString().getBytes().length)
                                .hash(preBlock.getHash())
                                .money(tx.getOutputs().get(0).getMoney())
                                .build();
                        minerBlocks.add(minerBlock);
                    } else {
                        MinerBlock minerBlock = MinerBlock.builder()
                                .height(block.getHeight())
                                .blockTime(block.getBlockTime())
                                .blockSize(block.toString().getBytes().length)
                                .hash(block.getHash())
                                .money(tx.getOutputs().get(0).getMoney())
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
