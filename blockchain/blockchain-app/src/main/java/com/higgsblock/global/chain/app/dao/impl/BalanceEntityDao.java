package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.dao.iface.IBalanceEntity;
import com.higgsblock.global.chain.common.utils.Money;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * The type Balance entity dao.
 *
 * @author yanghuadong
 * @date 2018 -07-02
 */
@Repository
public class BalanceEntityDao extends BaseDao<BalanceEntity> implements IBalanceEntity {
    /**
     * Add data to the tables specified by the database.
     *
     * @param balanceEntity entity
     * @return
     */
    @Override
    public int add(BalanceEntity balanceEntity) {
        String sql = "insert into t_balance (address,currency,amount)values (:address,:currency,:amount)";
        return super.add(balanceEntity, sql);
    }

    /**
     * Update the data for the database specified tables.
     *
     * @param balanceEntity entity
     * @return
     */
    @Override
    public int update(BalanceEntity balanceEntity) {
        String sql = "update t_balance set currency =:currency,amount=:amount where address=:address";
        return super.update(balanceEntity, sql);
    }

    /**
     * Deletes the contents of the database specified table.
     *
     * @param address entity
     * @return
     */
    @Override
    public <E> int delete(E address) {
        String sql = "delete from t_balance where address = :address";
        return super.delete(sql, ImmutableMap.of("address", address));
    }

    /**
     * Specify the contents of the table according to the field query database.
     *
     * @param address entity
     * @return
     */
    @Override
    public <E> BalanceEntity getByField(E address) {
        String sql = "select address,currency,amount from t_balance where address = :address limit 1";
        return super.getByField(sql, ImmutableMap.of("address", address));
    }

    /**
     * Query all data for the specified table.
     *
     * @return
     */
    @Override
    public List<BalanceEntity> findAll() {
        String sql = "select address,currency,amount from t_balance";
        return super.findAll(sql);
    }

    /**
     * Gets all balances.
     *
     * @return the all balances
     */
    @Override
    public Map<String, Money> getAllBalances() {
        Map<String, Money> balanceMap = Maps.newHashMap();
        List<BalanceEntity> balanceEntities = findAll();
        if (CollectionUtils.isEmpty(balanceEntities)) {
            return balanceMap;
        }

        balanceEntities.forEach(p -> balanceMap.putIfAbsent(p.getAddress(), p.getMoney()));
        return balanceMap;
    }

    /**
     * Gets balance.
     *
     * @param address  the address
     * @param currency the currency
     * @return the balance
     */
    @Override
    public BalanceEntity getBalance(String address, String currency) {
        String sql = "select address,currency,amount from t_balance where address = :address and currency = :currency limit 1";
        return super.getByField(sql, ImmutableMap.of("address", address, "currency", currency));
    }
}