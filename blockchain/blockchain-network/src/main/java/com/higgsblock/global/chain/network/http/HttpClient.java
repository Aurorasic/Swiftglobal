package com.higgsblock.global.chain.network.http;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
public class HttpClient {

    private static final long DEFAULT_TIMEOUT = 60L;
    private static final Cache<String, Retrofit> RETROFIT_CACHE = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build();

    private HttpClient() {
    }

    public static <T> T getApi(String ip, int port, Class<T> clazz) {
        return getRetrofit(ip, port).create(clazz);
    }

    private static Retrofit newRetrofit(String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(FastJsonConverterFactory.create())
                .client(CLIENT)
                .build();
    }

    private static Retrofit getRetrofit(String ip, int port) {
        String url = String.format("http://%s:%s/", ip, port);
        return RETROFIT_CACHE.get(url, HttpClient::newRetrofit);
    }
}
