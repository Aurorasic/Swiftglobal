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
public class GetLastBlockDataCreaterHttp {
    public static void main(String[] args) throws Exception {
        String ip = "192.168.10.71";
        int port = 8081;

        Map<String, Object> limitMap = Maps.newHashMap();
        limitMap.put("limit", 1L);

        try {
            if (UrlUtils.ipPortCheckout(ip, port)) {
                String url = UrlUtils.builderUrl(ip, port, UrlUtils.GET_RECENT_BLOCK_HEADER_LIST);
                String result = HttpClient.getSync(url, limitMap);
                ResponseData<BlockVO> resultData = JSON.parseObject(result, ResponseData.class);
                BlockVO blockVo = resultData.getData();
                System.out.println(JSONObject.toJSONString(blockVo, true));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
