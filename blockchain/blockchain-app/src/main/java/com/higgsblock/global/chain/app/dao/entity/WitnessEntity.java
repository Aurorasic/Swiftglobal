package com.higgsblock.global.chain.app.dao.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author yangshenghong
 * @date 2018-06-30
 */
@Data
@Entity
@Table(name = "t_witness", indexes = @Index(name = "index_pub_key", columnList = "pub_key", unique = true))
//@Table(name = "t_witness")
public class WitnessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "INTEGER")
    private int id;

    @Column(name = "pub_key", length = 90, columnDefinition = "VARCHAR")
    private String pubKey;

    @Column(name = "address", length = 50, columnDefinition = "VARCHAR")
    private String address;

    @Column(name = "socket_port", columnDefinition = "INTEGER")
    private int socketPort;

    @Column(name = "http_port", columnDefinition = "INTEGER")
    private int httpPort;
}
