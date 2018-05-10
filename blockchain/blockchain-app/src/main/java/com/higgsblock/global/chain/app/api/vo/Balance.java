package com.higgsblock.global.chain.app.api.vo;

import com.higgsblock.global.chain.common.utils.Money;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author kongyu
 * @date Created on 4/19/2018
 */
@NoArgsConstructor
@Data
public class Balance {
    @Getter
    private Money money;

    private Balance(Money money) {
        this.money = money;
    }

    public static BalanceBuilder builder(){
        return new BalanceBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BalanceBuilder {
        private Money money;

        public BalanceBuilder balance(Money balance) {
            this.money = balance;
            return this;
        }

        public Balance build() {
            return new Balance(money);
        }
    }
}
