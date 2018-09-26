package com.higgsblock.global.chain.app.dao.entity;

import com.higgsblock.global.chain.common.utils.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.util.List;

/**
 * @author yanghuadong
 * @date 2018-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@KeySpace("Balance")
public class BalanceEntity {
    @Id
    private String address;

    private List<Money> balances;
}