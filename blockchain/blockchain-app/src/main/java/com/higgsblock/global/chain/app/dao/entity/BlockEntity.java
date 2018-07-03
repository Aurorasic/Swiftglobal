package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
public class BlockEntity {
    private String blockHash;
    private long height;
    private String data;
}
