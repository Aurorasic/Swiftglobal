package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.IUTXORepository;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.service.IUTXOService;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author yuguojia
 * @date 2018/06/29
 **/
@Service
@Slf4j
public class UTXOService implements IUTXOService {
    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockIndexService blockIndexService;

    @Autowired
    private IUTXORepository iutxoRepository;
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

    @Override
    public void saveUTXO(UTXO utxo) {
        UTXOEntity entity = new UTXOEntity();
        TransactionOutput output = utxo.getOutput();

        entity.setAmount(output.getMoney().getValue());
        entity.setScriptType(output.getLockScript().getType());
        entity.setTransactionHash(utxo.getHash());
        entity.setOutIndex(utxo.getIndex());
        entity.setCurrency(output.getMoney().getCurrency());
        entity.setLockScript(output.getLockScript().getAddress());

        iutxoRepository.save(entity);
    }

    /**
     * get utxo only on confirm block chain
     *
     * @param utxoKey
     * @return
     */

    @Override
    public UTXO getUTXOOnBestChain(String utxoKey) {
        //return transactionIndexService.getUTXOOnBestChain(utxoKey);

        String[] keys = utxoKey.split("_");
        UTXOEntity entity = iutxoRepository.findByTransactionHashAndOutIndex(keys[0], Short.valueOf(keys[1]));

        if (entity == null) {
            return null;
        }

        TransactionOutput output = new TransactionOutput();

        LockScript lockScript = new LockScript();
        lockScript.setAddress(entity.getLockScript());
        lockScript.setType((short) entity.getScriptType());

        output.setMoney(new Money(entity.getAmount(), entity.getCurrency()));
        output.setLockScript(lockScript);

        UTXO utxo = new UTXO();
        utxo.setAddress(lockScript.getAddress());
        utxo.setHash(entity.getTransactionHash());
        utxo.setIndex(entity.getOutIndex());
        utxo.setOutput(output);

        return utxo;
    }

    /**
     * get utxo on confirm block chain and unconfirmed block chain(from the preBlockHash to best block)
     * from the max height first block
     *
     * @param preBlockHash
     * @param address
     * @param currency
     * @return
     */
    @Override
    public List<UTXO> getUnionUTXO(String preBlockHash, String address, String currency) {

        if (StringUtils.isEmpty(preBlockHash)) {
            BlockIndex lastBlockIndex = blockIndexService.getLastBlockIndex();
            String firstBlockHash = lastBlockIndex.getFirstBlockHash();
            if (StringUtils.isEmpty(firstBlockHash)) {
                throw new RuntimeException("error lastBlockIndex " + lastBlockIndex);
            }
            preBlockHash = firstBlockHash;
        }

        Map<String, UTXO> unconfirmedSpentUtxos = new HashMap<>();
        Map<String, UTXO> unconfirmedAddedUtxos = new HashMap<>();
        getUnionUTXOsRecurse(unconfirmedSpentUtxos, preBlockHash, false);
        getUnionUTXOsRecurse(unconfirmedAddedUtxos, preBlockHash, true);

        List<UTXO> bestAddedUtxoList = getUTXOsByAddress(address);

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
    @Override
    public UTXO getUnionUTXO(String preBlockHash, String utxoKey) {
        if (preBlockHash == null) {
            List<String> lastHeightBlockHashs = blockIndexService.getLastHeightBlockHashs();
            for (String tmpPreBlockHash : lastHeightBlockHashs) {
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

    @Override
    public boolean isRemovedUTXORecurse(String blockHash, String utxoKey) {
        if (StringUtils.isEmpty(blockHash)) {
            return false;
        }
        Map<String, UTXO> utxoMap = getAndLoadUnconfirmedUtxoMaps(blockHash);
        if (utxoMap == null || utxoMap.isEmpty()) {
            return false;
        }
        if (utxoMap.containsKey(utxoKey) && utxoMap.get(utxoKey) == null) {
            return true;
        }
        String preBlockHash = blockHashChainMap.get(blockHash);
        return isRemovedUTXORecurse(preBlockHash, utxoKey);
    }

    @Override
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

    @Override
    public List<UTXOEntity> findByLockScriptAndCurrency(String lockScript, String currency) {
        return iutxoRepository.findByLockScriptAndCurrency(lockScript, currency);
    }

    @Override
    public List<UTXO> getUTXOsByAddress(String addr) {
        if (null == addr) {
            throw new RuntimeException("addr is null");
        }

        List<UTXOEntity> entityList = iutxoRepository.findByLockScript(addr);
        if (CollectionUtils.isEmpty(entityList)) {
            return null;
        }

        List<UTXO> utxos = Lists.newArrayList();
        entityList.forEach(entity -> {
            Money money = new Money(entity.getAmount(), entity.getCurrency());
            LockScript lockScript = new LockScript();
            lockScript.setAddress(entity.getLockScript());
            lockScript.setType((short) entity.getScriptType());
            TransactionOutput output = new TransactionOutput();
            output.setMoney(money);
            output.setLockScript(lockScript);

            UTXO utxo = new UTXO();
            utxo.setHash(entity.getTransactionHash());
            utxo.setIndex(entity.getOutIndex());
            utxo.setAddress(entity.getLockScript());
            utxo.setOutput(output);
            utxos.add(utxo);
        });
        return utxos;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByTransactionHashAndOutIndex(String transactionHash, short outIndex) {
        iutxoRepository.deleteByTransactionHashAndOutIndex(transactionHash, outIndex);
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
                return new HashMap<>();
            }
            BlockIndex blockIndex = blockIndexService.getBlockIndexByHeight(block.getHeight());
            if (blockIndex == null || blockIndex.isBest(blockHash)) {
                return new HashMap<>();
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

    private String getPreBlockHash(String blockHash) {
        return blockHashChainMap.get(blockHash);
    }

    private boolean containsKey(String blockHash) {
        return blockHashChainMap.containsKey(blockHash) && unconfirmedUtxoMaps.containsKey(blockHash);
    }
}