package com.higgsblock.global.chain.app.dao.entity;

import com.higgsblock.global.chain.app.keyvalue.annotation.Index;
import lombok.Data;

import javax.persistence.*;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@Entity
@Table(name = "t_block_index")
public class BlockIndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Index
    @Column(name = "height", nullable = false, columnDefinition = "INTEGER")
    private long height;

    @Index
    @Column(name = "block_hash", nullable = false, length = 64, columnDefinition = "VARCHAR")
    private String blockHash;

    @Column(name = "is_best", nullable = false, columnDefinition = "INTEGER")
    private int isBest;

    @Column(name = "miner_address", nullable = false, length = 34, columnDefinition = "VARCHAR")
    private String minerAddress;
}

