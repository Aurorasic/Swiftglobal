package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author yuanjiantao
 * @date 6/30/2018
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_dpos")
public class DposEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "sn")
    private long sn;

    @Column(name = "addresses", nullable = false, length = 100, columnDefinition = "TEXT")
    private String addresses;

}
