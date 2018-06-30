package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
public class BlockIndexEntity {
    private long height;
    private String blockHash;
    private int isBest;
    private String minerAddress;
}
