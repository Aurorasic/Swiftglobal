package com.higgsblock.global.chain.app.discover;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author Su Jiulong
 * @date 2018-4-25
 */
public interface IAutoIp {
    @GET("/")
    Call<String> getAutoIp();
}
