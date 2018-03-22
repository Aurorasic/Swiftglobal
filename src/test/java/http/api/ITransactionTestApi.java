package http.api;

import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.transaction.Transaction;
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
    @POST("/transactions/send")
    Call<ResponseData<Boolean>> sendTx(@Body Transaction tx);

    @GET("/transactions/info")
    Call<ResponseData<List<Transaction>>> queryTxByTxHash(@Query("hash") String hash);

    @GET("/transactions/list")
    Call<ResponseData<List<Transaction>>> queryTxByPubKey(@Query("pubKey") String pubKey, @Query("op") String op);
}
