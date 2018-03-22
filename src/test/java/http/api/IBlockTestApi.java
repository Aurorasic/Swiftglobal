package http.api;

import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.Block;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface IBlockTestApi {

    @GET("/blocks/maxHeight")
    Call<ResponseData<Long>> getMaxHeight();

    @GET("blocks/lastBlock")
    Call<ResponseData<Block>> getLastBestBlock();

    @GET("/blocks/blocks")
    Call<ResponseData<List<Block>>> getBlocksByHeight(@Query("height") long height);
}
