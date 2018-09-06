package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * @author Su Jiulong
 * @date 2018-05-12
 */
@Deprecated
@Data
public class SpentTransactionOutIndexEntity {
    @Id
    private Long id;

    private String preTransactionHash;

    private short outIndex;

    private String nowTransactionHash;
}

