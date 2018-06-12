package com.higgsblock.global.chain.example.api;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.Block;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * @author kongyu
 * @date 2018-5-30 11:16
 */
public interface IBlockTestApi {

    /**
     * getMaxHeight
     *
     * @return
     */
    @GET("/v1.0.0/blocks/maxHeight")
    Call<ResponseData<Long>> getMaxHeight();

    /**
     * getLastBestBlock
     *
     * @return
     */
    @GET("/v1.0.0/blocks/lastBlock")
    Call<ResponseData<Block>> getLastBestBlock();

    /**
     * getBlocksByHeight
     *
     * @param height
     * @return
     */
    @GET("/v1.0.0/blocks/height")
    Call<ResponseData<List<Block>>> getBlocksByHeight(@Query("height") long height);

    /**
     * buildBlock
     *
     * @return
     */
    @GET("/v1.0.0/blocks/buildBlock")
    Call<ResponseData<Block>> buildBlock();
}
