package com.higgsblock.global.chain.app.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.math.BigDecimal;

/**
 * @author zhao xiaogang
 * @date 2018-10-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@KeySpace("token")
public class TokenEntity {
    @Id
    private String id;

    private String symbol;

    private String name;

    private int  accuracy;

    private BigDecimal totalOffering;
}
