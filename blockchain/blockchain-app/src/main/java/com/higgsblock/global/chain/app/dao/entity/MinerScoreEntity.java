package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yangshenghong
 * @date 2018-05-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinerScoreEntity {
    private String address;
    private int score;

    public MinerScoreEntity(String address) {
        this.address = address;
    }
}
