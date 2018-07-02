package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

/**
 * @author yangshenghong
 * @date 2018-05-07
 */
@Data
public class PeerEntity {
    private String pubKey;
    private String id;
    private String ip;
    private int socketPort;
    private int httpPort;
    private int version;
    private String signature;
    private int retry;
}
