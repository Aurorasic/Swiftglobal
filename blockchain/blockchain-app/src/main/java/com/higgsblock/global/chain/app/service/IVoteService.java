package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VoteTable;

/**
 * @author yangyi
 * @deta 2018/7/23
 * @description
 */
public interface IVoteService {


    long getHeight();

    /**
     * get the voting block by hash in current height voting block cache
     *
     * @param blockHash the block hash to query
     * @return the voting block
     */
    Block getVotingBlock(String blockHash);

    /**
     * init the witness task of the new height
     *
     * @param height the new height
     */
    void initWitnessTask(long height);

    /**
     * received new Original Block from miner
     *
     * @param block the block without witness sing
     */
    void addOriginalBlock(Block block);

    /**
     * received VoteTable from other witness
     *
     * @param otherVoteTable the voteTable to merge
     */
    void dealVoteTable(VoteTable otherVoteTable);

    /**
     * if the VoteTable can't be merge now,save the VoteTable into cache
     *
     * @param otherVoteTable the voteTable to merge
     */
    void updateVoteCache(VoteTable otherVoteTable);

    /**
     * check the block hash is in the voting block cache
     *
     * @param height
     * @param hash   block hash
     * @return true if exist or false;
     */
    boolean isExistInBlockCache(long height, String hash);
}
