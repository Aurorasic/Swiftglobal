package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

/**
 * @author Su Jiulong
 * @date 2018-05-12
 */
@Data
public class SpentTransactionOutIndexEntity {
    private String preTransactionHash;
    private short outIndex;
    private String nowTransactionHash;
}
