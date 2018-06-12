package com.higgsblock.global.browser.example.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.higgsblock.global.browser.utils.HttpClient;
import com.higgsblock.global.browser.vo.BlockVO;
import com.higgsblock.global.browser.vo.ResponseData;
import com.higgsblock.global.browser.example.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class GetBlocksByHeightCreaterHttp {

    public static void main(String[] args) throws Exception {
        String ip = "192.168.10.224";
        int port = 8081;
        long blockHeight = 15L;
        Map<String, Object> heightMap = Maps.newHashMap();
        heightMap.put("blockHeight", blockHeight);
        try {
            if (UrlUtils.ipPortCheckout(ip, port)) {
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.GET_BLOCK_BY_BLOCK_HEIGHT);
                String result = HttpClient.getSync(url, heightMap);
                ResponseData<BlockVO> resultData = JSON.parseObject(result, ResponseData.class);
                BlockVO blockVo = resultData.getData();
                System.out.println(JSONObject.toJSONString(blockVo, true));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
