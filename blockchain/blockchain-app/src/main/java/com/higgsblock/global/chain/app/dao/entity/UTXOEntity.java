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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_utxo")
public class UTXOEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "INTEGER")
    private Long id;

    @Column(name = "transaction_hash", nullable = false, columnDefinition = "VARCHAR")
    private String transactionHash;

    @Column(name = "out_index", nullable = false, columnDefinition = "INTEGER")
    private short outIndex;

    @Column(name = "amount", nullable = false, columnDefinition = "VARCHAR", length = 16)
    private String amount;

    @Column(name = "currency", nullable = false, columnDefinition = "VARCHAR", length = 8)
    private String currency;

    @Column(name = "script_type", nullable = false, columnDefinition = "INTEGER")
    private int scriptType;

    @Column(name = "lock_script", nullable = false, columnDefinition = "VARCHAR")
    private String lockScript;
}

