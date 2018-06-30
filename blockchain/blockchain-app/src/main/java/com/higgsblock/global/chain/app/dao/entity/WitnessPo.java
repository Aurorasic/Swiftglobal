package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

/**
 * @author yangshenghong
 * @date 2018-06-30
 */
@Data
public class WitnessPo {
    private int id;
    private String pubKey;
    private String address;
    private int socketPort;
    private int httpPort;
}
