package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.service.impl.BlockDaoService;
import com.higgsblock.global.chain.app.service.impl.BlockIdxDaoService;
import com.higgsblock.global.chain.app.service.impl.TransDaoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author yuguojia
 * @date 2018/06/29
 **/
@Service
@Slf4j
public class UTXODaoServiceProxy {
    @Autowired
    private TransDaoService transDaoService;
    @Autowired
    private BlockDaoService blockDaoService;
    @Autowired
    private BlockIdxDaoService blockIdxDaoService;

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
     * get utxo only on confirm block chain
     *
     * @param utxoKey
     * @return
     */
    public UTXO getUTXOOnBestChain(String utxoKey) {
        return transDaoService.getUTXO(utxoKey);
    }

    /**
     * get utxo on confirm block chain and unconfirmed block chain(from the preBlockHash to best block)
     * from the max height first block
     *
     * @param address
     * @return
     */
    public List<UTXO> getUnionUTXO(String address) {
        BlockIndex lastBlockIndex = blockIdxDaoService.getLastBlockIndex();
        String firstBlockHash = lastBlockIndex.getFirstBlockHash();
        if (StringUtils.isEmpty(firstBlockHash)) {
            throw new RuntimeException("error lastBlockIndex " + lastBlockIndex);
        }

        Map<String, UTXO> unconfirmedSpentUtxos = new HashMap<>();
        Map<String, UTXO> unconfirmedAddedUtxos = new HashMap<>();
        getUnionUTXOsRecurse(unconfirmedSpentUtxos, address, false);
        getUnionUTXOsRecurse(unconfirmedAddedUtxos, address, true);

        List<UTXO> bestAddedUtxoList = transDaoService.getUTXOsByAddress(address);

        Map<String, UTXO> allUtxoMap = new HashMap<>();
        for (UTXO utxo : bestAddedUtxoList) {
            if (unconfirmedSpentUtxos.containsKey(utxo.getKey())) {
                continue;
            }
            allUtxoMap.put(utxo.getKey(), utxo);
        }
        for (UTXO utxo : unconfirmedAddedUtxos.values()) {
            if (unconfirmedSpentUtxos.containsKey(utxo.getKey())) {
                continue;
            }
            allUtxoMap.put(utxo.getKey(), utxo);
        }

        List<UTXO> result = new LinkedList();
        for (UTXO utxo : allUtxoMap.values()) {
            if (utxo == null || !StringUtils.equals(address, utxo.getAddress())) {
                continue;
            }
            result.add(utxo);
        }
        return result;
    }

    /**
     * get utxo on confirm block chain and unconfirmed block chain(from the preBlockHash to best block)
     *
     * @param preBlockHash
     * @param utxoKey
     * @return
     */
    public UTXO getUnionUTXO(String preBlockHash, String utxoKey) {
        UTXO utxo = transDaoService.getUTXO(utxoKey);
        if (utxo == null) {
            utxo = getUTXORecurse(preBlockHash, utxoKey);
        } else {
            boolean isRemovedOnUnconfirmedChain = isRemovedUTXORecurse(preBlockHash, utxoKey);
            if (isRemovedOnUnconfirmedChain) {
                return null;
            }
        }
        return utxo;
    }

    private void getUnionUTXOsRecurse(Map result, String blockHash, boolean isToGetAdded) {
        if (StringUtils.isEmpty(blockHash)) {
            return;
        }
        Map<String, UTXO> utxoMap = unconfirmedUtxoMaps.get(blockHash);
        if (utxoMap == null) {
            //there is no utxo cache, load this block and build new utxo map to cache
            Block block = blockDaoService.getBlockByHash(blockHash);
            if (block == null) {
                return;
            }
            BlockIndex blockIndex = blockIdxDaoService.getBlockIndexByHeight(block.getHeight());
            if (blockIndex == null || blockIndex.isBest(blockHash)) {
                return;
            }
            utxoMap = buildUTXOMap(block);
            unconfirmedUtxoMaps.put(blockHash, utxoMap);
            blockHashChainMap.put(blockHash, block.getPrevBlockHash());
        }

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

    public UTXO getUTXORecurse(String blockHash, String utxoKey) {
        if (StringUtils.isEmpty(blockHash)) {
            return null;
        }
        Map<String, UTXO> utxoMap = unconfirmedUtxoMaps.get(blockHash);
        if (utxoMap == null) {
            //there is no utxo cache, load this block and build new utxo map to cache
            Block block = blockDaoService.getBlockByHash(blockHash);
            if (block == null) {
                return null;
            }
            BlockIndex blockIndex = blockIdxDaoService.getBlockIndexByHeight(block.getHeight());
            if (blockIndex == null || blockIndex.isBest(blockHash)) {
                return null;
            }
            utxoMap = buildUTXOMap(block);
            unconfirmedUtxoMaps.put(blockHash, utxoMap);
            blockHashChainMap.put(blockHash, block.getPrevBlockHash());
        }
        if (utxoMap.containsKey(utxoKey)) {
            return utxoMap.get(utxoKey);
        }
        String preBlockHash = blockHashChainMap.get(blockHash);
        return getUTXORecurse(preBlockHash, utxoKey);
    }

    public boolean isRemovedUTXORecurse(String blockHash, String utxoKey) {
        if (StringUtils.isEmpty(blockHash)) {
            return false;
        }
        Map<String, UTXO> utxoMap = unconfirmedUtxoMaps.get(blockHash);
        if (utxoMap == null || utxoMap.isEmpty()) {
            return false;
        }
        if (utxoMap.containsKey(utxoKey) && utxoMap.get(utxoKey) == null) {
            return true;
        }
        String preBlockHash = blockHashChainMap.get(blockHash);
        return isRemovedUTXORecurse(preBlockHash, utxoKey);
    }

    public void addNewBlock(Block newBestBlock, Block newBlock) {
        if (newBestBlock != null) {
            //remove cached blocks info for the blocks of on the new best block height
            BlockIndex bestBlockIndex = blockIdxDaoService.getBlockIndexByHeight(newBestBlock.getHeight());
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

    private String getPreBlockHash(String blockHash) {
        return blockHashChainMap.get(blockHash);
    }

    private boolean containsKey(String blockHash) {
        return blockHashChainMap.containsKey(blockHash) && unconfirmedUtxoMaps.containsKey(blockHash);
    }
}