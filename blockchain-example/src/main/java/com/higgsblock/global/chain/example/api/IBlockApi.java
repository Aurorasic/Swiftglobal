package com.higgsblock.global.chain.example.api;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Provide the latest height of the HTTP interface query node externally
 *
 * @author kongyu
 * @date 2018-5-30
 */
public interface IBlockApi {

    /**
     * getMaxHeight
     *
     * @return
     */
    @GET("/v1.0.0/blocks/maxHeight")
    Call<ResponseData<Long>> getMaxHeight();
}
