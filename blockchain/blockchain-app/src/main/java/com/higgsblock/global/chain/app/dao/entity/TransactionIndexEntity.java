package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_transaction_index")
public class TransactionIndexEntity {
    @Column(name = "transaction_hash")
    private String transactionHash;
    @Column(name = "block_hash")
    private String blockHash;
    @Column(name = "transaction_index")
    private short transactionIndex;
}
