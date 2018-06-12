package com.higgsblock.global.chain.example.test;

import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import com.higgsblock.global.chain.example.api.IBlockTestApi;
import com.higgsblock.global.chain.network.http.HttpClient;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author kongyu
 * @date 2018-05-21
 */
public class PackageBlockHttp {
    private final static String IP = "127.0.0.1";
    private final static int PORT = 8084;

    public static void main(String[] args) throws Exception {
        Block block = buildBlock(IP, PORT);

        System.out.println("-----------------------------------------------");
        System.out.println(JSON.toJSONString(block, true));
    }

    public static Block buildBlock(String ip, int port) {
        if (null == ip) {
            throw new RuntimeException("params is null");
        }
        if (port <= 0) {
            throw new RuntimeException("port is invalid");
        }

        IBlockTestApi api = HttpClient.getApi(ip, port, IBlockTestApi.class);
        ResponseData<Block> responseData = null;
        try {
            responseData = api.buildBlock().execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (StringUtils.equals(RespCodeEnum.SUCCESS.getCode(), responseData.getRespCode())) {
            return responseData.getData();
        }
        return null;
    }
}
