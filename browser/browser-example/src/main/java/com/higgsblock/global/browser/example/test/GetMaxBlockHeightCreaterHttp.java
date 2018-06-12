package com.higgsblock.global.browser.example.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.higgsblock.global.browser.app.utils.HttpClient;
import com.higgsblock.global.browser.app.vo.BlockVO;
import com.higgsblock.global.browser.app.vo.ResponseData;
import com.higgsblock.global.browser.example.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;


/**
 * @author baizhengwen
 * @date 2018/3/5
 */

@Slf4j
public class GetMaxBlockHeightCreaterHttp {

    public static void main(String[] args) throws Exception {
        String pubKey = "03d25c3b5fc0f48f300879d36597104860b0bc2c66bdc5e8bafb0426fd1cf9f396";
        String ip = "192.168.10.224";
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
