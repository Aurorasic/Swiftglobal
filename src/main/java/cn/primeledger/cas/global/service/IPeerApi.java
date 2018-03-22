package cn.primeledger.cas.global.service;

import cn.primeledger.cas.global.p2p.Peer;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
public interface IPeerApi {

    @GET("/peers/list")
    Call<List<Peer>> list();

    @GET("/peers/neighbors")
    Call<List<Peer>> getNeighbors();

    //TODO:  bai   wrap when return null
    @GET("peers/query")
    Call<Peer> get(@Query("address") String address);

    @GET("peers/querylist")
    Call<List<Peer>> getList(@Query("address[]") String[] address);

    @POST("/peers/register")
    Call<Boolean> register(@Body Peer peer);
}
