package http.api;

import cn.primeledger.cas.global.api.vo.ResponseData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author kongyu
 * @date 2018-3-20
 */
public interface IMinerApi {
    @GET("/miners/count")
    Call<ResponseData<Long>> statisticsMinersNumber();

    @GET("/miners/check")
    Call<ResponseData<Boolean>> isMinerByPubKey(@Query("pubKey") String pubKey);
}
