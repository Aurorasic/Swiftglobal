package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WitnessBlockEntity {
    private long height;
    private String blockHash;

    public WitnessBlockEntity(long height) {
        this.height = height;
    }
}
