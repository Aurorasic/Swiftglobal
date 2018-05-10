package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import retrofit2.Call;
import retrofit2.http.POST;

/**
 * @author yuguojia
 * @date 2018/4/26
 */
public interface IBlockApi {

    @POST("/blocks/signedBlock")
    Call<Block> getSignedBlock(long height);
}
