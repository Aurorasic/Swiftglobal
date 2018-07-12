package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UTXOEntity {
    @Column(name = "transaction_hash")
    private String transactionHash;
    @Column(name = "out_index")
    private short outIndex;
    @Column(name = "amount")
    private String amount;
    @Column(name = "currency")
    private String currency;
    @Column(name = "script_type")
    private int scriptType;
    @Column(name = "lock_script")
    private String lockScript;
}
