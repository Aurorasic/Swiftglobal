package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Su Jiulong
 * @date 2018-05-12
 */
@Data
@Entity
@Table(name = "t_spent_transaction_out_index")
public class SpentTransactionOutIndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "pre_transaction_hash", nullable = false, columnDefinition = "VARCHAR")
    private String preTransactionHash;

    @Column(name = "out_index", nullable = false, columnDefinition = "INTEGER")
    private short outIndex;

    @Column(name = "now_transaction_hash", nullable = false, columnDefinition = "VARCHAR")
    private String nowTransactionHash;
}

