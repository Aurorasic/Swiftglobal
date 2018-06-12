package com.higgsblock.global.chain.example.api;

import com.higgsblock.global.chain.app.api.vo.ConnectionInfo;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-04-09
 */
public interface IConnectionApi {

    /**
     * all
     * @return
     */
    @GET("/connections/list")
    Call<List<ConnectionInfo>> all();
}
