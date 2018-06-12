package com.higgsblock.global.chain.example.api;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

/**
 * @author kongyu
 * @date 2018-03-19
 */
public interface ITransactionTestApi {
    /**
     * send tx
     *
     * @param tx
     * @return
     */
    @POST("/v1.0.0/transactions/send")
    Call<ResponseData<Boolean>> sendTx(@Body Transaction tx);

    /**
     * query UTXO
     *
     * @param address
     * @return
     */
    @GET("/v1.0.0/transactions/queryUTXO")
    Call<ResponseData<List<UTXO>>> queryUTXO(@Query("address") String address);
}
