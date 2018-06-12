package com.higgsblock.global.chain.example.api;

import com.higgsblock.global.chain.app.api.vo.MinerBlock;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * @author kongyu
 * @date 2018-3-20
 */
public interface IMinerApi {
    /**
     *statisticsMinersNumber
     * @return
     */
    @GET("/v1.0.0/miners/count")
    Call<ResponseData<Long>> statisticsMinersNumber();

    /**
     *isMinerByPubKey
     * @param pubKey
     * @return
     */
    @GET("/v1.0.0/miners/isMiner")
    Call<ResponseData<Boolean>> isMinerByPubKey(@Query("pubKey") String pubKey);

    /**
     * statisticsMinerBlocks
     * @param pubKey
     * @return
     */
    @GET("/v1.0.0/miners/blocks")
    Call<ResponseData<List<MinerBlock>>> statisticsMinerBlocks(@Query("pubKey") String pubKey);
}
