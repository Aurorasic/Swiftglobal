package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.IBalanceRepository;
import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.service.IBalanceService;
import com.higgsblock.global.chain.app.service.IBestUTXOService;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private IBestUTXOService bestUTXOService;

    @Autowired
    private IBlockIndexService blockIndexService;
    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

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
        Map<String, Map<String, Money>> minusMap = getMinusMap(block);
        Map<String, Map<String, Money>> plusMap = getPlusMap(block);
        plusMap.forEach((k, v) -> {
            if (minusMap.containsKey(k)) {
                Map<String, Money> minusCurrencyMap = minusMap.get(k);
                v.keySet().forEach(currency -> {
                    v.compute(currency, (k1, v1) -> {
                        Money minusMoney = minusCurrencyMap.get(currency);

                        return v1;
                    });
                });
            }
        });


        Set<String> addresses = Sets.newLinkedHashSet(minusMap.keySet());
        addresses.addAll(plusMap.keySet());

        Map<String, Map<String, Money>> map = Maps.newHashMap();
        for (String address : addresses) {
            Map<String, Money> currencyMap = get(address);
            if (MapUtils.isEmpty(currencyMap)) {
                map.put(address, plusMap.get(address));
            } else {

            }
        }
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

        Money result = new Money("0");
        for (Money money : allAddedBalanceList) {
            if (StringUtils.isNotEmpty(currency) && !StringUtils.equals(currency, money.getCurrency())) {
                continue;
            }
            result.add(money);
        }
        return null;
    }

    private Map<String, Map<String, Money>> getMinusMap(Block block) {
        Map<String, Map<String, Money>> minusMap = Maps.newHashMap();
        for (Transaction tx : block.getTransactions()) {
            for (String spendUtxoKey : tx.getSpendUTXOKeys()) {
                UTXO spendUtxo = bestUTXOService.getUTXOByKey(spendUtxoKey);
                if (null == spendUtxo) {
                    LOGGER.warn("cannot find utxo:{}", spendUtxoKey);
                    continue;
                }

                minusMap.compute(spendUtxo.getAddress(), (k, v) -> {
                    if (null == v) {
                        v = Maps.newHashMap();
                    }

                    v.put(spendUtxo.getOutput().getMoney().getCurrency(), spendUtxo.getOutput().getMoney());
                    return v;
                });
            }
        }

        return minusMap;
    }

    private Map<String, Map<String, Money>> getPlusMap(Block block) {
        Map<String, Map<String, Money>> plusMap = Maps.newHashMap();
        for (Transaction tx : block.getTransactions()) {
            for (UTXO addUtxo : tx.getAddedUTXOs()) {
                plusMap.compute(addUtxo.getAddress(), (k, v) -> {
                    if (null == v) {
                        v = Maps.newHashMap();
                    }

                    Money money = addUtxo.getOutput().getMoney();
                    v.put(money.getCurrency(), money);
                    return v;
                });
            }
        }

        return plusMap;
    }
}