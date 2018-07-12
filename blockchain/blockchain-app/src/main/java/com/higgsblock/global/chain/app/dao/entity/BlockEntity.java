package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@Entity
@Table(name = "t_block")
public class BlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long id;

    @Column(name = "block_hash", nullable = false, columnDefinition = "VARCHAR", length = 64)
    private String blockHash;

    @Column(name = "height", nullable = false, columnDefinition = "INTEGER")
    private long height;

    @Column(name = "data", nullable = false, columnDefinition = "TEXT")
    private String data;
}
