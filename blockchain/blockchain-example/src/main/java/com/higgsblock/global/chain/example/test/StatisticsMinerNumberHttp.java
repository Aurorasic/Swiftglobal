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
public class StatisticsMinerNumberHttp {
    private final static String IP = "192.168.10.71";
    private final static int PORT = 8081;

    public static void main(String[] args) throws Exception {
        Long minerNumber = statisticsMinersNumber(IP, PORT);

        System.out.println("-----------------------------------------------");
        System.out.println("当前网站中矿工的数量为 = " + minerNumber);
    }

    public static Long statisticsMinersNumber(String ip, int port) {
        if (null == ip) {
            throw new RuntimeException("params is null");
        }
        if (port <= 0) {
            throw new RuntimeException("port is invalid");
        }

        IMinerApi api = HttpClient.getApi(ip, port, IMinerApi.class);
        ResponseData<Long> responseData = null;
        try {
            responseData = api.statisticsMinersNumber().execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (StringUtils.equals(RespCodeEnum.SUCCESS.getCode(), responseData.getRespCode())) {
            return responseData.getData();
        }
        return null;
    }
}
