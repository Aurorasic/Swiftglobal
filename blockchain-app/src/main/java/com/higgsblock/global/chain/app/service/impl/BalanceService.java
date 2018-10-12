package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.IBalanceRepository;
import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.service.IBalanceService;
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

    /**
     * The Balance repository.
     */
    @Autowired
    private IBalanceRepository balanceRepository;

    /**
     * The Utxo service proxy.
     */
    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    /**
     * The Block index service.
     */
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
        Map<String, Money> map = getBalanceByAddress(address);
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
    public Map<String, Money> getBalanceByAddress(String address) {
        BalanceEntity entity = balanceRepository.findOne(address);
        if (null == entity || CollectionUtils.isEmpty(entity.getBalances())) {
            return Maps.newHashMap();
        }

        Map<String, Money> maps = Maps.newHashMapWithExpectedSize(entity.getBalances().size());
        entity.getBalances().forEach(p -> maps.put(p.getCurrency(), p));
        return maps;
    }

    /**
     * Plus balance.
     *
     * @param utxo the utxo
     */
    @Override
    public void plusBalance(UTXO utxo) {
        Map<String, Money> balanceMap = getBalanceByAddress(utxo.getAddress());
        if (MapUtils.isEmpty(balanceMap) || !balanceMap.containsKey(utxo.getCurrency())) {
            balanceMap.put(utxo.getAddress(), utxo.getOutput().getMoney());
            save(utxo.getAddress(), balanceMap);
            return;
        }

        balanceMap.compute(utxo.getCurrency(), (currency, money) -> {
            if (null == money) {
                money = new Money(0, utxo.getCurrency());
            }

            money.add(utxo.getOutput().getMoney());
            return money;
        });

        save(utxo.getAddress(), balanceMap);
    }

    /**
     * Minus balance.
     *
     * @param utxo the utxo
     */
    @Override
    public void minusBalance(UTXO utxo) {
        Map<String, Money> balanceMap = getBalanceByAddress(utxo.getAddress());
        if (MapUtils.isEmpty(balanceMap)) {
            throw new IllegalStateException("can't find address balance:" + utxo.getAddress());
        }

        if (!balanceMap.containsKey(utxo.getCurrency())) {
            throw new IllegalStateException("can't find currency balance:" + utxo.getCurrency());
        }

        balanceMap.compute(utxo.getCurrency(), (currency, money) -> {
            if (null == money) {
                throw new RuntimeException("error balance address" + utxo.getAddress());
            }
            return money.subtract(utxo.getOutput().getMoney());
        });
        save(utxo.getAddress(), balanceMap);
    }

    /**
     * Save.
     *
     * @param address    the address
     * @param balanceMap the balance map
     */
    private void save(String address, Map<String, Money> balanceMap) {
        BalanceEntity entity = new BalanceEntity(address, balanceMap.values().stream().collect(Collectors.toList()));
        balanceRepository.save(entity);
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