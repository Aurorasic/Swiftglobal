package com.higgsblock.global.chain.example.test;

import com.alibaba.fastjson.JSONObject;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.network.http.HttpClient;
import com.higgsblock.global.chain.example.api.IBlockTestApi;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class GetLastBlockDataCreaterHttp {
    public static void main(String[] args) throws Exception {
//        while (true){
        String ip = "192.168.10.71";
        int port = 8081;

        IBlockTestApi iBlockApi = HttpClient.getApi(ip, port, IBlockTestApi.class);
        ResponseData<Block> responseData = iBlockApi.getLastBestBlock().execute().body();
        System.out.println(JSONObject.toJSONString(responseData.getData(), true));
        System.out.println("区块最高高度：" + responseData.getData().getHeight());
        TimeUnit.SECONDS.sleep(4);
    }
//    }
}
