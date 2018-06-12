package com.higgsblock.global.chain.example.test;

import com.alibaba.fastjson.JSONObject;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.network.http.HttpClient;
import com.higgsblock.global.chain.example.api.IBlockTestApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class GetBlocksByHeightCreaterHttp {


    public static void main(String[] args) throws Exception {
        String ip = "192.168.10.224";
        int port = 8081;
        long blockHeight = 15L;

        IBlockTestApi iBlockApi = HttpClient.getApi(ip, port, IBlockTestApi.class);
        ResponseData<List<Block>> responseData = iBlockApi.getBlocksByHeight(blockHeight).execute().body();
        List<Block> blocks = responseData.getData();
        System.out.println(JSONObject.toJSONString(blocks, true));
        System.out.println("区块数量：" + responseData.getData().size());
    }

}
