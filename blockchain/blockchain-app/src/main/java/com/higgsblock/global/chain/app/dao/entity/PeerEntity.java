package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author yangshenghong
 * @date 2018-05-07
 */
@Data
@Entity
@Table(name = "t_peer")
public class PeerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "p_id")
    private long pid;

    @Column(name = "pub_key", nullable = false, columnDefinition = "VARCHAR", length = 66)
    private String pubKey;

    @Column(name = "id", nullable = false, columnDefinition = "VARCHAR", length = 34)
    private String id;

    @Column(name = "ip", nullable = false, columnDefinition = "VARCHAR", length = 15)
    private String ip;

    @Column(name = "socket_port", nullable = false, columnDefinition = "INTEGER")
    private int socketPort;

    @Column(name = "http_port", nullable = false, columnDefinition = "INTEGER")
    private int httpPort;

    @Column(name = "version", nullable = false, columnDefinition = "INTEGER")
    private int version;

    @Column(nullable = false, columnDefinition = "VARCHAR", length = 88)
    private String signature;

    @Column(nullable = false, columnDefinition = "INTEGER")
    private int retry;
}

