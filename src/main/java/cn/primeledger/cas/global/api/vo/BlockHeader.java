package cn.primeledger.cas.global.api.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date Created on 3/19/2018
 */
@NoArgsConstructor
@Data
public class BlockHeader {


    /**
     * block height begin with 1
     */
    private long height;

    /**
     * the hash of this block
     */
    private String hash;

    /**
     * the timestamp of this block created
     */
    private long blockTime;

    /**
     * the hash of prev block
     */
    private String prevBlockHash;

//    /**
//     * public keys and signatures of pairs.
//     * The first pk and sig is the miner's
//     */
//    private List<BlockWitness> blockWitnesses = new ArrayList<>();
//
//
//    private List<BlockWitness> blockMiner = new ArrayList<>();
//
//    private List<String> nodes;
}
