package cn.primeledger.cas.global.service;

import cn.primeledger.cas.global.network.Peer;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
public interface IPeerApi {

    @GET("/peers/list")
    Call<List<Peer>> list();
}
