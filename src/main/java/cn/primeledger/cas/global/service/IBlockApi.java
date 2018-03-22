package cn.primeledger.cas.global.service;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.consensus.sign.model.WitnessSign;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IBlockApi {
    @POST("/blocks/sign")
    Call<WitnessSign> getWitnessSign(@Body Block block);
}
