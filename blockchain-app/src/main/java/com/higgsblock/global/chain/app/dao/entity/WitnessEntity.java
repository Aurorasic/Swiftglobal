package com.higgsblock.global.chain.app.dao.entity;

import com.higgsblock.global.chain.app.keyvalue.annotation.Index;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * @author yangshenghong
 * @date 2018-06-30
 */
@Data
@KeySpace("Witeness")
public class WitnessEntity {
    @Id
    private Integer id;

    @Index
    private String pubKey;

    private String address;

    private int socketPort;

    private int httpPort;
}