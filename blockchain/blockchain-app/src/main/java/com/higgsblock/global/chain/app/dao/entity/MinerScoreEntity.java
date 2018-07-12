package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author yangshenghong
 * @date 2018-05-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_score")
public class MinerScoreEntity {
    @Id
    @Column
    private String address;
    @Column
    private int score;

    public MinerScoreEntity(String address) {
        this.address = address;
    }
}
