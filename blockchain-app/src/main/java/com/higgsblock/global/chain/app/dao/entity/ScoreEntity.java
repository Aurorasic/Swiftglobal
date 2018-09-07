package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * @author yuanjiantao
 * @date 6/30/2018
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@KeySpace("Score")
public class ScoreEntity {

    @Id
    private String address;

    private Integer score;
}

