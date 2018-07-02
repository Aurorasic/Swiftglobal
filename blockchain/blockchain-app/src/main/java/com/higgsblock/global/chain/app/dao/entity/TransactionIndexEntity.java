package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionIndexEntity {
    private String transactionHash;
    private String blockHash;
    private short transactionIndex;
}
