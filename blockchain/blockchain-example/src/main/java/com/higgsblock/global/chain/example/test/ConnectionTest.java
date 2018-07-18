package com.higgsblock.global.chain.example.test;

import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.app.api.vo.ConnectionVO;
import com.higgsblock.global.chain.example.api.IConnectionApi;
import com.higgsblock.global.chain.network.http.HttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/4/9
 */
public class ConnectionTest {
    private final static int MAX_NUMBER = 5;

    public static void main(String[] args) throws Exception {
        String ip = "192.168.1.168";
        int port = 8081;
        for (int i = 0; i < MAX_NUMBER; i++) {
            IConnectionApi api = HttpClient.getApi(ip, port, IConnectionApi.class);
            List<ConnectionVO> connections = api.all().execute().body();
            System.out.println(JSON.toJSONString(connections, true));
            System.out.println(ip + ":" + port + " 总连接数：" + connections.size());
            port++;
            TimeUnit.SECONDS.sleep(3);
        }
    }
}
