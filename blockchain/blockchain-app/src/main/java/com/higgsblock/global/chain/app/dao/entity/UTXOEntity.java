package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UTXOEntity {
    private String transactionHash;
    private short outIndex;
    private String amount;
    private String currency;
    private int scriptType;
    private String lockScript;
}
