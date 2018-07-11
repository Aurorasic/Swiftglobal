package com.higgsblock.global.chain.example.api;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author kongyu
 * @date 2018-5-30 11:16
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
