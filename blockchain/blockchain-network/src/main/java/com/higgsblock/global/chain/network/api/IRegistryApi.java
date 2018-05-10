package com.higgsblock.global.chain.network.api;

import com.higgsblock.global.chain.network.Peer;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
public interface IRegistryApi {

    @GET("/registry/peers")
    Call<List<Peer>> peers();
}
