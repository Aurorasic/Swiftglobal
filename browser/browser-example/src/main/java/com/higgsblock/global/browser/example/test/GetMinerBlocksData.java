package com.higgsblock.global.browser.example.test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.higgsblock.global.browser.utils.HttpClient;
import com.higgsblock.global.browser.vo.ResponseData;
import com.higgsblock.global.browser.vo.RewardBlockVO;
import com.higgsblock.global.browser.example.utils.UrlUtils;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author kongyu
 * @date 2018-5-30 11:16
 */
public class GetMinerBlocksData {
    private final static String IP = "127.0.0.1";
    private final static int PORT = 8084;
    /**
     * 转出公钥
     */
    private static String pubKey = "03d5eb9f503d18e9c5d998e4dd9a3b5e43b7dccfa20957c7ee32ede8117d1a1a10";

    public static void main(String[] args) throws Exception {
        List<RewardBlockVO> minerBlocks = getMinerBlockDataByPubKey(IP, PORT, pubKey);

        System.out.println("-----------------------------------------------");
        if (CollectionUtils.isEmpty(minerBlocks)) {
            System.out.println("size is 0");
            return;
        }
        for (RewardBlockVO rewardBlockVo : minerBlocks) {
            System.out.println(JSON.toJSONString(rewardBlockVo, true));
        }
    }

    public static List<RewardBlockVO> getMinerBlockDataByPubKey(String ip, int port, String pubKey) {
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
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.GET_MINER_BLOCKS);
                String result = HttpClient.getSync(url, pubkeyMap);
                ResponseData<RewardBlockVO> resultData = JSON.parseObject(result, ResponseData.class);
                List<RewardBlockVO> rewardBlockVos = (List<RewardBlockVO>) resultData.getData();
                return rewardBlockVos;
            }
        } catch (IOException e) {

        }

        return null;
    }
}
