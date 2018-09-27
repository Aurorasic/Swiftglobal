package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.IBalanceRepository;
import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.service.IBalanceService;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Balance service.
 *
 * @author yanghuadong
 * @date 2018 -09-26
 */
@Service
@Slf4j
public class BalanceService implements IBalanceService {

    @Autowired
    private IBalanceRepository balanceRepository;

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    @Autowired
    private IBlockIndexService blockIndexService;

    /**
     * Gets balance.
     *
     * @param address  the address
     * @param currency the currency
     * @return the money
     */
    @Override
    public Money getBalanceOnBest(String address, String currency) {
        Map<String, Money> map = get(address);
        Money result = map.get(currency);
        if (result == null) {
            return new Money(0, currency);
        }

        return result;
    }

    /**
     * Get map.
     *
     * @param address the address
     * @return the map
     */
    @Override
    public Map<String, Money> get(String address) {
        BalanceEntity entity = balanceRepository.findOne(address);
        if (null == entity || CollectionUtils.isEmpty(entity.getBalances())) {
            return Maps.newHashMap();
        }

        Map<String, Money> maps = Maps.newHashMapWithExpectedSize(entity.getBalances().size());
        entity.getBalances().forEach(p -> maps.put(p.getCurrency(), p));
        return maps;
    }

    /**
     * Save.
     *
     * @param block the block
     */
    @Override
    public void save(Block block) {
        if (null == block) {
            return;
        }

        Map<String, Map<String, Money>> minusMap = getBalanceMap(block.getSpendUTXOs());
        Map<String, Map<String, Money>> plusMap = getBalanceMap(block.getAddedUTXOs());

        // compute balance
        Set<String> addressSet = Sets.newHashSet(minusMap.keySet());
        addressSet.addAll(plusMap.keySet());
        Map<String, Map<String, Money>> resultMap = Maps.newHashMap();
        for (String address : addressSet) {
            if (!resultMap.containsKey(address)) {
                Map<String, Money> dbCurrencyMap = get(address);
                resultMap.put(address, dbCurrencyMap);
            }

            Map<String, Money> resultCurrencyMap = resultMap.get(address);
            Map<String, Money> minusCurrencyMap = minusMap.getOrDefault(address, Maps.newHashMap());
            Map<String, Money> plusCurrencyMap = plusMap.getOrDefault(address, Maps.newHashMap());
            LOGGER.debug("save balance,{}:address={},dbMoney={},minusMoney={},plusMoney={}", block.getSimpleInfo(), address, JSON.toJSONString(resultCurrencyMap), JSON.toJSONString(minusCurrencyMap), JSON.toJSONString(plusCurrencyMap));
            // minus balance
            minusCurrencyMap.forEach((currency, minusValue) -> {
                resultCurrencyMap.compute(currency, (k1, dbValue) -> {
                    if (null == dbValue) {
                        dbValue = new Money(0, currency);
                    }

                    dbValue = dbValue.subtract(minusValue);
                    return dbValue;
                });
            });
            // plus balance
            plusCurrencyMap.forEach((currency, plusValue) -> {
                resultCurrencyMap.compute(currency, (k1, dbValue) -> {
                    if (null == dbValue) {
                        dbValue = new Money(0, currency);
                    }

                    dbValue = dbValue.add(plusValue);
                    return dbValue;
                });
            });
        }

        LOGGER.debug("save balance,{},resultMap={}", block.getSimpleInfo(), JSON.toJSONString(resultMap));
        // save balance
        resultMap.forEach((k, v) -> {
            BalanceEntity entity = new BalanceEntity(k, v.values().stream().collect(Collectors.toList()));
            balanceRepository.save(entity);
        });
        LOGGER.debug("save blance success,{}", block.getSimpleInfo());
    }

    private Map<String, Map<String, Money>> getBalanceMap(List<UTXO> utxos) {
        Map<String, Map<String, Money>> map = Maps.newHashMap();
        for (UTXO utxo : utxos) {
            map.compute(utxo.getAddress(), (k, v) -> {
                if (null == v) {
                    v = Maps.newHashMap();
                }

                Money money = utxo.getOutput().getMoney();
                v.put(money.getCurrency(), new Money(money.getValue(), money.getCurrency()));
                return v;
            });
        }

        return map;
    }

    /**
     * get balance on confirm block chain and unconfirmed block chain(from the preBlockHash to best block)
     * from the max height first block
     *
     * @param preBlockHash
     * @param address
     * @param currency
     * @return
     */
    @Override
    public Money getUnionBalance(String preBlockHash, String address, String currency) {
        if (StringUtils.isEmpty(preBlockHash)) {
            BlockIndex lastBlockIndex = blockIndexService.getLastBlockIndex();
            String firstBlockHash = lastBlockIndex.getFirstBlockHash();
            if (StringUtils.isEmpty(firstBlockHash)) {
                throw new RuntimeException("error lastBlockIndex " + lastBlockIndex);
            }
            preBlockHash = firstBlockHash;
        }

        //get confirmed chain balance
        Money balanceMoney = getBalanceOnBest(address, currency);

        //get no confirmed chain balance
        Money unconfirmedBalanceMoney = utxoServiceProxy.getUnconfirmedBalance(preBlockHash, address, currency);

        List<Money> allAddedBalanceList = new LinkedList<>();
        allAddedBalanceList.add(balanceMoney);
        allAddedBalanceList.add(unconfirmedBalanceMoney);

        Money result = new Money(0, currency);
        for (Money money : allAddedBalanceList) {
            if (StringUtils.isNotEmpty(currency) && !StringUtils.equals(currency, money.getCurrency())) {
                continue;
            }
            result.add(money);
        }
        return result;
    }
}