package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @author yangshenghong
 * @date 2018-05-07
 */
@Data
public class PeerEntity {
    @Id
    @Column(name = "pub_key")
    private String pubKey;
    @Column(name = "id")
    private String id;
    @Column(name = "ip")
    private String ip;
    @Column(name = "socket_port")
    private int socketPort;
    @Column(name = "http_port")
    private int httpPort;
    @Column(name = "version")
    private int version;
    @Column(name = "signature")
    private String signature;
    @Column(name = "retry")
    private int retry;
}
