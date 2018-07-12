package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Su Jiulong
 * @date 2018-05-12
 */
@Data
@Entity
@Table(name = "t_spent_transaction_out_index")
public class SpentTransactionOutIndexEntity {
    @Column(name = "pre_transaction_hash")
    private String preTransactionHash;
    @Column(name = "out_index")
    private short outIndex;
    @Column(name = "now_transaction_hash")
    private String nowTransactionHash;
}
