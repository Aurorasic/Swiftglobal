package com.higgsblock.global.browser.example.test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.higgsblock.global.browser.utils.HttpClient;
import com.higgsblock.global.browser.example.utils.UrlUtils;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author kongyu
 * @date 2018/3/20
 */

@Slf4j
public class GetTransactionsByPubKeyHttp {

    private final static String IP = "127.0.0.1";
    private final static int PORT = 8084;
    /**
     * 转出公钥
     */
    private static String pubKey = "03d5eb9f503d18e9c5d998e4dd9a3b5e43b7dccfa20957c7ee32ede8117d1a1a10";

    public static void main(String[] args) throws Exception {
        String op = null;

        List<Transaction> list = getTransactionsByPubKey(IP, PORT, pubKey, op);
        if (CollectionUtils.isEmpty(list)) {
            System.out.println("The public key has no corresponding transaction information.");
        }
        System.out.println("-----------------------------------------------");
        System.out.println(JSON.toJSONString(list, true));
    }

    public static List<Transaction> getTransactionsByPubKey(String ip, int port, String pubKey, String op) {
        if (null == ip || null == pubKey) {
            throw new RuntimeException("params is null");
        }
        if (port <= 0) {
            throw new RuntimeException("port is invalid");
        }

        Map<String, Object> paramsMap = Maps.newHashMap();
        paramsMap.put("pubKey", pubKey);
        paramsMap.put("op", op);

        try {
            if (UrlUtils.ipPortCheckout(ip, port)) {
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.GET_TRANSACTIONS_BY_PUB_KEY);
                String result = HttpClient.getSync(url, paramsMap);
                List<Transaction> transactions = (List<Transaction>) JSON.parseObject(result, Transaction.class);
                return transactions;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
