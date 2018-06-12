package com.higgsblock.global.chain.network.http;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
public class HttpClient {

    /**
     * The constant DEFAULT_PORT. 80
     */
    public static final int DEFAULT_PORT = 80;

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

    /**
     * Get string.
     *
     * @param url  the url
     * @param port the port
     * @return the string
     * @throws IOException the io exception
     */
    public static String get(String url, Integer port) throws IOException {
        if (null == port) {
            port = DEFAULT_PORT;
        }

        url = getFormat(url, port);
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = CLIENT.newCall(request).execute();
        return response.body().string().trim();
    }

    /**
     * Get string.
     *
     * @param url the url
     * @return the string
     * @throws IOException the io exception
     */
    public static String get(String url) throws IOException {
        return get(url, null);
    }

    private static Retrofit newRetrofit(String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(FastJsonConverterFactory.create())
                .client(CLIENT)
                .build();
    }

    private static Retrofit getRetrofit(String ip, int port) {
        String url = getFormat(ip, port);
        return RETROFIT_CACHE.get(url, HttpClient::newRetrofit);
    }

    private static String getFormat(String ip, int port) {
        return String.format("http://%s:%s/", ip, port);
    }
}
