package com.higgsblock.global.chain.example.test;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.example.api.IBlockApi;
import com.higgsblock.global.chain.network.http.HttpClient;
import lombok.extern.slf4j.Slf4j;


/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class GetMaxBlockHeightTest {
    private final static int MAX_PORT = 8086;

    public static void main(String[] args) throws Exception {
        String ip = "localhost";
        int port = 8081;

        while (port < MAX_PORT) {
            IBlockApi iBlockApi = HttpClient.getApi(ip, port, IBlockApi.class);
            ResponseData<Long> responseData = iBlockApi.getMaxHeight().execute().body();
            System.out.println("The maxHeight is :" + responseData.getData());
            port++;
        }

    }

}
