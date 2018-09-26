package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.IBalanceRepository;
import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.service.IBalanceService;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
        if (MapUtils.isEmpty(map)) {
            return null;
        }

        return map.get(currency);
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
            return null;
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

        for (Map.Entry<String, Map<String, Money>> entry : plusMap.entrySet()) {
            String address = entry.getKey();
            if (!minusMap.containsKey(address)) {
                continue;
            }

            Map<String, Money> minusCurrencyMap = minusMap.get(address);
            Map<String, Money> dbCurrencyMap = get(address);
            entry.getValue().keySet().forEach(currency -> {
                entry.getValue().compute(currency, (k1, v1) -> {
                    Money minusMoney = minusCurrencyMap.get(currency);
                    if (null != minusMoney) {
                        v1.subtract(minusMoney);
                    }

                    if (MapUtils.isNotEmpty(dbCurrencyMap) && dbCurrencyMap.containsKey(currency)) {
                        v1.add(dbCurrencyMap.get(currency));
                    }

                    return v1;
                });
            });

            BalanceEntity entity = new BalanceEntity(address, entry.getValue().values().stream().collect(Collectors.toList()));
            balanceRepository.save(entity);
        }
    }

    private Map<String, Map<String, Money>> getBalanceMap(List<UTXO> utxos) {
        Map<String, Map<String, Money>> map = Maps.newHashMap();
        for (UTXO utxo : utxos) {
            map.compute(utxo.getAddress(), (k, v) -> {
                if (null == v) {
                    v = Maps.newHashMap();
                }

                Money money = utxo.getOutput().getMoney();
                v.put(money.getCurrency(), new Money(money.getCurrency(), money.getValue()));
                return v;
            });
        }

        return map;
    }
}