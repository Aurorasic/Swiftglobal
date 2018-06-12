package com.higgsblock.global.browser.example.test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.higgsblock.global.browser.app.utils.HttpClient;
import com.higgsblock.global.browser.app.vo.ResponseData;
import com.higgsblock.global.browser.example.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * @author kongyu
 * @date 2018/3/20
 */
@Slf4j
public class CheckPubKeyIsMinerHttp {
    private final static String IP = "127.0.0.1";
    private final static int PORT = 8084;
    /**
     * 转出公钥
     */
    private static String pubKey = "03d5eb9f503d18e9c5d998e4dd9a3b5e43b7dccfa20957c7ee32ede8117d1a1a10";

    public static void main(String[] args) throws Exception {
        Boolean isMiner = isMinerByPubKey(IP, PORT, pubKey);

        System.out.println("-----------------------------------------------");
        System.out.println("公钥：" + pubKey + " 是否为矿工 = " + isMiner);
    }

    public static Boolean isMinerByPubKey(String ip, int port, String pubKey) {
        if (null == ip || null == pubKey) {
            throw new RuntimeException("params is null");
        }
        if (port <= 0) {
            throw new RuntimeException("port is invalid");
        }
        Map<String, Object> pubkeyMap = Maps.newHashMap();
        pubkeyMap.put("pubKey", pubKey);

        try {
            if (UrlUtils.ipPortCheckout(ip, port)) {
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.CHECK_PUB_KEY_IS_MINER);
                String result = HttpClient.getSync(url, pubkeyMap);
                ResponseData<Boolean> resultData = JSON.parseObject(result, ResponseData.class);
                return resultData.getData();
            }
        } catch (IOException e) {

        }
        return null;
    }
}
