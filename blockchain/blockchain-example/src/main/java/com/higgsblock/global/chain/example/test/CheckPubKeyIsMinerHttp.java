package com.higgsblock.global.chain.example.test;

import com.higgsblock.global.chain.app.api.vo.ResponseData;
import com.higgsblock.global.chain.app.constants.RespCodeEnum;
import com.higgsblock.global.chain.network.http.HttpClient;
import com.higgsblock.global.chain.example.api.IMinerApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author kongyu
 * @date 2018/3/20
 */
@Slf4j
public class CheckPubKeyIsMinerHttp {
    private final static String IP = "127.0.0.1";
    private final static int PORT = 8084;
    /**转出公钥*/
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

        IMinerApi api = HttpClient.getApi(ip, port, IMinerApi.class);
        ResponseData<Boolean> responseData = null;
        try {
            responseData = api.isMinerByPubKey(pubKey).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (StringUtils.equals(RespCodeEnum.SUCCESS.getCode(), responseData.getRespCode())) {
            return responseData.getData();
        }
        return null;
    }
}
