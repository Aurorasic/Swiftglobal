package com.higgsblock.global.chain.app.utils;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;

/**
 * @author chenjiawei
 * @date 2018-03-28
 */
public class JsonSizeCounter implements SizeCounter {
    @Override
    public long calculateSize(Object o) {
        try {
            return JSON.toJSONString(o).getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Long.MAX_VALUE;
    }
}
