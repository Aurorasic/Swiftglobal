package cn.primeledger.cas.global.test;

import cn.primeledger.cas.global.network.http.client.HttpClient;
import cn.primeledger.cas.global.service.IPeerApi;
import com.alibaba.fastjson.JSON;

import java.io.IOException;

/**
 * @author baizhengwen
 * @date 2018/3/19
 */
public class HttpTest {
    public static void main(String[] args) throws IOException {
        IPeerApi api = HttpClient.getApi("localhost", 8081, IPeerApi.class);
        Object data = null;
        data = api.list().execute().body();
        System.out.println(JSON.toJSONString(data, true));

        data = api.get("11").execute().body();
        System.out.println(JSON.toJSONString(data, true));
    }
}
