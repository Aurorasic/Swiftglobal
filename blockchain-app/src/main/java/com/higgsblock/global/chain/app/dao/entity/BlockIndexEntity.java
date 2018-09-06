package com.higgsblock.global.chain.app.dao.entity;

import com.higgsblock.global.chain.app.keyvalue.annotation.Index;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * @author Su Jiulong
 * @date 2018-05-07
 */
@Data
@KeySpace("BlockIndex")
public class BlockIndexEntity {
    @Id
    private Long id;

    @Index
    private long height;

    @Index
    private String blockHash;

    private int isBest;

    private String minerAddress;
}
