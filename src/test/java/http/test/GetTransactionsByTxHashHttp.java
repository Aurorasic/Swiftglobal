package http.test;

import cn.primeledger.cas.global.api.vo.ResponseData;
import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.network.http.client.HttpClient;
import com.alibaba.fastjson.JSON;
import http.api.ITransactionTestApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author kongyu
 * @date 2018/3/20
 */

@Slf4j
public class GetTransactionsByTxHashHttp {

    //转出公钥
    private static String pubKey = "03aea965d1106f7a2927b62ad59d96aa8731b33eb07b2a6346fd2451b0cca2ba7e";

    private final static String IP = "127.0.0.1";
    private final static int PORT = 8084;

    public static void main(String[] args) throws Exception {
        String txHash = "7c664eba8f4973a1417065a6de1704743a343c863b443db82b000bcd98471e51";

        List<Transaction> list = getTransactionsByTxHash(IP, PORT, txHash);
        if (CollectionUtils.isEmpty(list)) {
            System.out.println("The public key has no corresponding transaction information.");
        }
        System.out.println("-----------------------------------------------");
        System.out.println(JSON.toJSONString(list, true));
    }

    public static List<Transaction> getTransactionsByTxHash(String ip, int port, String txHash) {
        if (null == ip || null == pubKey) {
            throw new RuntimeException("params is null");
        }

        ITransactionTestApi api = HttpClient.getApi(ip, port, ITransactionTestApi.class);
        ResponseData<List<Transaction>> responseData = null;
        try {
            responseData = api.queryTxByTxHash(txHash).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(JSON.toJSONString(responseData, true));
        return responseData.getData();
    }
}
