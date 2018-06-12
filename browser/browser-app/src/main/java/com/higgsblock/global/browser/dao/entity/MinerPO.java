package com.higgsblock.global.browser.dao.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Data
public class MinerPO {
    /**
     * On the primary key
     */
    private long id;
    /**
     * money
     */
    private String amount;
    /**
     * Miner address
     */
    private String address;
}
