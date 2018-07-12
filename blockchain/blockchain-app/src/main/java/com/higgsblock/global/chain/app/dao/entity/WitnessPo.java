package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author yangshenghong
 * @date 2018-06-30
 */
@Data
@Entity
@Table(name = "t_witness")
public class WitnessPo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "pub_key")
    private String pubKey;
    @Column(name = "address")
    private String address;
    @Column(name = "socket_port")
    private int socketPort;
    @Column(name = "http_port")
    private int httpPort;
}
