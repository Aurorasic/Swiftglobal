package http.test;

import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.network.http.client.HttpClient;
import com.alibaba.fastjson.JSONObject;
import http.api.IBlockTestApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class GetBlocksByHeightCreaterHttp {


    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        int port = 8081;
        long blockHeight = 8L;

        IBlockTestApi iBlockApi = HttpClient.getApi(ip, port, IBlockTestApi.class);
        ResponseData<List<Block>> responseData = iBlockApi.getBlocksByHeight(blockHeight).execute().body();
        List<Block> blocks = responseData.getData();
        System.out.println("The blocksByHeight is :" + JSONObject.toJSONString(blocks));

    }

}
