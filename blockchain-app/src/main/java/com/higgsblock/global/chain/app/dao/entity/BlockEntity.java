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
@KeySpace("Block")
public class BlockEntity {

    @Id
    private Long id;

    @Index
    private String blockHash;

    @Index
    private long height;

    private String data;
}
