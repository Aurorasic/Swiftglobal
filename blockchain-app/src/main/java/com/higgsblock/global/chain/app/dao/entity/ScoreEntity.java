package com.higgsblock.global.chain.app.dao.entity;

import com.higgsblock.global.chain.app.keyvalue.annotation.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author yuanjiantao
 * @date 6/30/2018
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_score")
public class ScoreEntity {

    public ScoreEntity(String address, Integer score) {
        this.address = address;
        this.score = score;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Index
    @Column(name = "address", columnDefinition = "VARCHAR", length = 34, nullable = false)
    private String address;

    @Column(name = "score", columnDefinition = "INTEGER", nullable = false)
    private Integer score;
}

