package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Utxo service that handle utxos both on best block chain and on unconfirmed block chains
 *
 * @author yuguojia
 * @date 2018/07/19
 **/
@Service
@Slf4j
public class UTXOServiceProxy {
    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockIndexService blockIndexService;

    @Autowired
    private BestUTXOService bestUtxoService;

    /**
     * key1: unconfirmed blockhash
     * key2: utxo key
     * value: utxo that this block added or spent, if value is null, it means the utxo is removed/spent
     */
    private Map<String, Map<String, UTXO>> unconfirmedUtxoMaps = new HashMap<>(16);

    /**
     * key:blockhash
     * value:preblockhash
     */
    private Map<String, String> blockHashChainMap = new HashMap<>(16);


    /**
     * get utxo on confirm block chain and unconfirmed block chain(from the preBlockHash to best block)
     * from the max height first block
     *
     * @param preBlockHash
     * @param address
     * @param currency
     * @return
     */
    public List<UTXO> getUnionUTXO(String preBlockHash, String address, String currency) {

        if (StringUtils.isEmpty(preBlockHash)) {
            BlockIndex lastBlockIndex = blockIndexService.getLastBlockIndex();
            String firstBlockHash = lastBlockIndex.getFirstBlockHash();
            if (StringUtils.isEmpty(firstBlockHash)) {
                throw new RuntimeException("error lastBlockIndex " + lastBlockIndex);
            }
            preBlockHash = firstBlockHash;
        }

        Map<String, UTXO> unconfirmedSpentUtxos = Maps.newHashMap();
        Map<String, UTXO> unconfirmedAddedUtxos = Maps.newHashMap();
        getUnionUTXOsRecurse(unconfirmedSpentUtxos, preBlockHash, false);
        getUnionUTXOsRecurse(unconfirmedAddedUtxos, preBlockHash, true);

        List<UTXO> bestAddedUtxoList = bestUtxoService.getUTXOsByAddress(address);

        List<UTXO> allAddedUtxoList = new LinkedList<>();
        allAddedUtxoList.addAll(bestAddedUtxoList);
        allAddedUtxoList.addAll(unconfirmedAddedUtxos.values());
        Set<UTXO> result = new HashSet<>();
        for (UTXO utxo : allAddedUtxoList) {
            if (StringUtils.isNotEmpty(currency) &&
                    !StringUtils.equals(currency, utxo.getCurrency())) {
                continue;
            }
            if (!StringUtils.equals(address, utxo.getAddress()) ||
                    unconfirmedSpentUtxos.containsKey(utxo.getKey())) {
                continue;
            }

            result.add(utxo);
        }

        return new LinkedList<>(result);
    }

    /**
     * get utxo on confirm block chain and unconfirmed block chain(from the preBlockHash to best block)
     *
     * @param preBlockHash
     * @param utxoKey
     * @return
     */
    public UTXO getUnionUTXO(String preBlockHash, String utxoKey) {
        if (preBlockHash == null) {
            List<String> lastHeightBlockHashs = blockIndexService.getLastHeightBlockHashs();
            for (String tmpPreBlockHash : lastHeightBlockHashs) {
                if (isUsedOnUnconfirmedChain(tmpPreBlockHash, utxoKey)) {
                    return null;
                }
                UTXO utxo = getUnconfirmedUTXORecurse(tmpPreBlockHash, utxoKey);
                if (utxo != null) {
                    return utxo;
                }
                utxo = getUTXOOnBestChain(utxoKey);
                if (utxo != null) {
                    return utxo;
                }
            }
        } else {
            if (isUsedOnUnconfirmedChain(preBlockHash, utxoKey)) {
                return null;
            }
            UTXO utxo = getUnconfirmedUTXORecurse(preBlockHash, utxoKey);
            if (utxo != null) {
                return utxo;
            }
            utxo = getUTXOOnBestChain(utxoKey);
            if (utxo != null) {
                return utxo;
            }
        }

        return null;
    }

    private boolean isUsedOnUnconfirmedChain(String preBlockHash, String utxoKey) {
        Map<String, UTXO> unconfirmedSpentUtxos = Maps.newHashMap();
        getUnionUTXOsRecurse(unconfirmedSpentUtxos, preBlockHash, false);
        if (unconfirmedSpentUtxos.containsKey(utxoKey)) {
            return true;
        }
        return false;
    }

    /**
     * get utxo only on confirm block chain
     *
     * @param utxoKey
     * @return
     */
    public UTXO getUTXOOnBestChain(String utxoKey) {
        return bestUtxoService.getUTXOByKey(utxoKey);
    }

    private void getUnionUTXOsRecurse(Map result, String blockHash, boolean isToGetAdded) {
        if (StringUtils.isEmpty(blockHash)) {
            return;
        }
        Map<String, UTXO> utxoMap = getAndLoadUnconfirmedUtxoMaps(blockHash);
        Set<String> keySet = utxoMap.keySet();
        for (String key : keySet) {
            UTXO utxo = utxoMap.get(key);
            if (isToGetAdded && utxo != null) {
                result.put(key, utxo);
            } else if (!isToGetAdded && utxo == null) {
                result.put(key, null);
            }
        }

        String preBlockHash = blockHashChainMap.get(blockHash);
        getUnionUTXOsRecurse(result, preBlockHash, isToGetAdded);
    }

    private UTXO getUnconfirmedUTXORecurse(String blockHash, String utxoKey) {
        if (StringUtils.isEmpty(blockHash)) {
            return null;
        }
        Map<String, UTXO> utxoMap = getAndLoadUnconfirmedUtxoMaps(blockHash);
        if (utxoMap.containsKey(utxoKey)) {
            return utxoMap.get(utxoKey);
        }
        String preBlockHash = blockHashChainMap.get(blockHash);
        return getUnconfirmedUTXORecurse(preBlockHash, utxoKey);
    }

    public void addNewBlock(Block newBestBlock, Block newBlock) {
        if (newBestBlock != null) {
            //remove cached blocks info for the blocks of on the new best block height
            BlockIndex bestBlockIndex = blockIndexService.getBlockIndexByHeight(newBestBlock.getHeight());
            ArrayList<String> bestBlockHashs = bestBlockIndex.getBlockHashs();
            for (String blockHash : bestBlockHashs) {
                remove(blockHash);
            }
        }

        if (newBlock != null) {
            Map utxoMap = buildUTXOMap(newBlock);
            put(newBlock.getPrevBlockHash(), newBlock.getHash(), utxoMap);
        }
    }


    /**
     * get the utxos on the block if the block is in cache, else load the block and calculate
     *
     * @param blockHash
     * @return if no element return empty hashmap
     */
    private Map<String, UTXO> getAndLoadUnconfirmedUtxoMaps(String blockHash) {
        Map<String, UTXO> utxoMap = unconfirmedUtxoMaps.get(blockHash);
        if (utxoMap == null) {
            //there is no utxo cache, load this block and build new utxo map to cache
            Block block = blockService.getBlockByHash(blockHash);
            if (block == null) {
                return Maps.newHashMap();
            }
            BlockIndex blockIndex = blockIndexService.getBlockIndexByHeight(block.getHeight());
            if (blockIndex == null || blockIndex.isBest(blockHash)) {
                return Maps.newHashMap();
            }
            utxoMap = buildUTXOMap(block);
            unconfirmedUtxoMaps.put(blockHash, utxoMap);
            blockHashChainMap.put(blockHash, block.getPrevBlockHash());
        }
        return utxoMap;
    }

    private Map buildUTXOMap(Block block) {
        Map utxoMap = new HashMap<>(32);
        List<String> spendUTXOKeys = block.getSpendUTXOKeys();
        List<UTXO> addedUTXOs = block.getAddedUTXOs();
        for (String spendUTXOKey : spendUTXOKeys) {
            utxoMap.put(spendUTXOKey, null);
        }
        for (UTXO newUTXO : addedUTXOs) {
            utxoMap.put(newUTXO.getKey(), newUTXO);
        }
        return utxoMap;
    }

    private void put(String preBlockHash, String blockHash, Map utxoMap) {
        blockHashChainMap.put(blockHash, preBlockHash);
        unconfirmedUtxoMaps.put(blockHash, utxoMap);
    }

    private void remove(String blockHash) {
        blockHashChainMap.remove(blockHash);
        unconfirmedUtxoMaps.remove(blockHash);
    }
}