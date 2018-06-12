package com.higgsblock.global.browser.app.vo;

import lombok.Data;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-26
 */
@Data
public class BlockVO {
    /**
     * Block height
     */
    private long height;
    /**
     * Block hash
     */
    private String blockHash;
    /**
     * Block generation time
     */
    private String blockTime;
    /**
     * Previous block hash
     */
    private String preBlockHash;

    /**
     * transaction list
     */
    private List<TransactionVO> transactions;

    private List<BlockWitnessVO> blockWitnesses;

    private List<BlockWitnessVO> blockMiner;

    private List<String> nodes;
}
