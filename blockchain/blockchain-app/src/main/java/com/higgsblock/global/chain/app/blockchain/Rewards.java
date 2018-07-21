package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.common.utils.Money;
import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-21 18:33
 **/
@Getter
@Setter
public class Rewards {
    public static final long WITNESS_NUM = 11;
    
    private Money totalFee;
    private Money minerTotal;
    private Money topTenSingleWitnessMoney;
    private Money lastWitnessMoney;
    private Money totalMoney;

    /**
     * check count whether true
     *
     * @return countMoney == totalFee add total rewards return true else return false
     */
    public boolean check() {
        Money countMoney = new Money();
        countMoney.add(minerTotal);
        countMoney.add(new Money(topTenSingleWitnessMoney.getValue()).multiply(WITNESS_NUM - 1));
        countMoney.add(lastWitnessMoney);
        return totalMoney.compareTo(countMoney) == 0;
    }
}