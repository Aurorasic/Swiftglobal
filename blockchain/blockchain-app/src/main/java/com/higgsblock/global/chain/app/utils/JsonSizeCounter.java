package com.higgsblock.global.chain.app.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

/**
 * @author chenjiawei
 * @date 2018-03-28
 */
@Slf4j
public class JsonSizeCounter implements SizeCounter {
    @Override
    public long calculateSize(Object o) {
        try {
            return JSON.toJSONString(o).getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            LOGGER.info(e.getMessage(), e);
        }
        return Long.MAX_VALUE;
    }
}
