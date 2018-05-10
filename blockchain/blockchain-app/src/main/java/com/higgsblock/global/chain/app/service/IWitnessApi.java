package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.Collection;

/**
 * @author yangyi
 * @deta 2018/4/26
 * @description
 */
public interface IWitnessApi {

    /**
     * miner send original/candidate block to witness for select best block
     *
     * @param block
     * @return
     */
    @POST("/witness/sendBlockToWitness")
    Call<Boolean> sendBlockToWitness(@Body Block block);

    @POST("/witness/getRecommendBlock")
    Call<Block> getRecommendBlock(@Body long height);

    @POST("/witness/getCandidateBlocksByHeight")
    Call<Collection<Block>> getCandidateBlocksByHeight(@Body long height);

    /**
     * step 1: get the witness all candidate block hashs
     * @param height
     * @return
     */
    @POST("/witness/getCandidateBlockHashs")
    Call<Collection<String>> getCandidateBlockHashs(@Body long height);

    /**
     * step 2: if i haven't some blocks of the witness's candidate block, to fetch these blocks
     * @param blockHashs
     * @return
     */
    @POST("/witness/getCandidateBlocksByHashs")
    Call<Collection<Block>> getCandidateBlocksByHashs(@Body Collection<String> blockHashs);

    /**
     * step 3: if i have some blocks that witness haven't, send these blocks to that witness
     * @param blocks
     * @return
     */
    @POST("/witness/putBlocksToWitness")
    Call<Boolean> putBlocksToWitness(@Body Collection<Block> blocks);
}
