package com.higgsblock.global.browser.example.test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.higgsblock.global.browser.app.utils.HttpClient;
import com.higgsblock.global.browser.example.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * @author kongyu
 * @date 2018/3/20
 */

@Slf4j
public class StatisticsMinerNumberHttp {
    private final static String IP = "192.168.10.71";
    private final static int PORT = 8081;
    private final static String PUB_KEY = "";

    public static void main(String[] args) throws Exception {
        Long minerNumber = statisticsMinersNumber(IP, PORT, PUB_KEY);

        System.out.println("-----------------------------------------------");
        System.out.println("当前网站中矿工的数量为 = " + minerNumber);
    }

    public static Long statisticsMinersNumber(String ip, int port, String pubKey) {
        if (null == ip) {
            throw new RuntimeException("params is null");
        }
        if (port <= 0) {
            throw new RuntimeException("port is invalid");
        }

        Map<String, Object> pubKeyMap = Maps.newHashMap();
        pubKeyMap.put("pubKey", pubKey);

        try {
            if (UrlUtils.ipPortCheckout(ip, port)) {
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.GET_MINERS_COUNT);
                String result = HttpClient.getSync(url, pubKeyMap);
                Long count = JSON.parseObject(result, Long.class);
                return count;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }
}
