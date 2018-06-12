package com.higgsblock.global.browser.utils;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author kongyu
 * @date 2018-5-22 17:33
 */
public class HttpClient {
    private static final long DEFAULT_TIMEOUT = 20L;
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build();

    /**
     * 同步的Get请求
     *
     * @param url
     * @return responseStr
     * @throws IOException
     */
    public static String getSync(String url, Map<String, Object> params) throws IOException {
        StringBuffer result = new StringBuffer();
        result.append("http://").append(url).append("?");
        Set<String> keys = params.keySet();
        for (String key : keys) {
            result.append(key).append("=").append(params.get(key)).append("&");
        }
        url = result.substring(0, result.length() - 1);
        // 创建一个Request
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = CLIENT.newCall(request).execute();
        // 将response转化成String
        return response.body().string().trim();
    }

    public static <T> String doPost(String url, T data) {
        url = "http://" + url;
        okhttp3.RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , JSONObject.toJSONString(data));

        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        String result = null;
        try {
            result = CLIENT.newCall(request).execute().body().string().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

}
