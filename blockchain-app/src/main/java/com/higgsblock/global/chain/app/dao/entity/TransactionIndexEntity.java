package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@KeySpace("TxIndex")
public class TransactionIndexEntity {

    @Id
    private String transactionHash;

    private String blockHash;

    private short transactionIndex;
}

