package com.higgsblock.global.browser.service.bo;

import lombok.Data;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-24
 */
@Data
public class BlockBO {
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
    private List<TransactionBO> transactions;

    private List<BlockWitnessBO> blockWitnesses;

    private List<BlockWitnessBO> blockMiner;

    private List<String> nodes;
}
