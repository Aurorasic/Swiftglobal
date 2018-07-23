package com.higgsblock.global.chain.app.service;

        import com.higgsblock.global.chain.app.blockchain.Block;

/**
 * @author yangyi
 * @deta 2018/7/23
 * @description
 */
public interface IOriginalBlockService {

    /**
     * after package a new block,send the block to witness
     *
     * @param block the block that haven't witness sign
     */
    void sendOriginBlockToWitness(Block block);
}
