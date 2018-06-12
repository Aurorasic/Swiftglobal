package com.higgsblock.global.chain.app.sync;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 *
 * @author liuweizhen
 * @date 2018-05-09
 */
public class MapTest {

    public static void main(String[] args) {

        Map<String, String> map = Maps.newConcurrentMap();
        map.put("kkkk", "vvvv");

        Thread[] ts = new Thread[100];
        for (int i = 0; i < 100; ++i) {

            ts[i] = new Thread(() -> {
                System.out.println(map.remove("kkkk"));
                ;
            });
        }

        for (int i = 0; i < 100; ++i) {
            ts[i].start();
        }
    }
}
