package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "INTEGER")
    private long id;

    @Column(name = "transaction_hash", nullable = false, columnDefinition = "VARCHAR")
    private String transactionHash;

    @Column(name = "block_hash", nullable = false, length = 64, columnDefinition = "VARCHAR")
    private String blockHash;

    @Column(name = "transaction_index", nullable = false, columnDefinition = "INTEGER")
    private short transactionIndex;
}

