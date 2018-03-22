package http.api;

import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.transaction.UTXO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * @author kongyu
 * @date 2018-03-20
 */
public interface IUTXOTestApi {
    @GET("/transactions/query")
    Call<ResponseData<List<UTXO>>> query(@Query("address") String address);
}
