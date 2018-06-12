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
public class GetTransactionsByTxHashHttp {

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
        if (null == ip || null == txHash) {
            throw new RuntimeException("params is null");
        }
        if (port <= 0) {
            throw new RuntimeException("port is invalid");
        }

        Map<String, Object> txHashMap = Maps.newHashMap();
        txHashMap.put("txHash", txHash);

        try {
            if (UrlUtils.ipPortCheckout(ip, port)) {
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.GET_TRANSACTIONS_BY_TX_HASH);
                String result = HttpClient.getSync(url, txHashMap);
                List<Transaction> transactions = (List<Transaction>) JSON.parseObject(result, Transaction.class);
                return transactions;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
