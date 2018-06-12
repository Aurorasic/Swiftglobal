package com.higgsblock.global.chain.example.test;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.example.api.IBlockTestApi;
import com.higgsblock.global.chain.network.http.HttpClient;
import lombok.extern.slf4j.Slf4j;


/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class GetMaxBlockHeightCreaterHttp {
    private final static int MAX_PORT = 8086;

    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        int port = 8081;

        while (port < MAX_PORT) {
            IBlockTestApi iBlockApi = HttpClient.getApi(ip, port, IBlockTestApi.class);
            ResponseData<Long> responseData = iBlockApi.getMaxHeight().execute().body();
            System.out.println("The maxHeight is :" + responseData.getData());
            port++;
        }

    }

}
