package http.test;

import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.constants.RespCodeEnum;
import cn.primeledger.cas.global.network.http.client.HttpClient;
import http.api.IMinerApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author kongyu
 * @date 2018/3/20
 */
@Slf4j
public class CheckPubKeyIsMinerHttp {
    //转出公钥
//    private static String pubKey = "03aea965d1106f7a2927b62ad59d96aa8731b33eb07b2a6346fd2451b0cca2ba7e";
    private static String pubKey = "028a186b944c76d7ca626a3ba8ba9609d46de318affb48ee760a0c3336f426d741";
    private final static String IP = "127.0.0.1";
    private final static int PORT = 8084;

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
