package http.test;

import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.network.http.client.HttpClient;
import com.alibaba.fastjson.JSONObject;
import http.api.IBlockTestApi;
import lombok.extern.slf4j.Slf4j;

/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class GetLastBlockDataCreaterHttp {


    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        int port = 8081;

        IBlockTestApi iBlockApi = HttpClient.getApi(ip, port, IBlockTestApi.class);
        ResponseData<Block> responseData = iBlockApi.getLastBestBlock().execute().body();
        System.out.println("The LastData is :" + JSONObject.toJSONString(responseData.getData()));

    }

}
