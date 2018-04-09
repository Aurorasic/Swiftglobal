package cn.primeledger.cas.global.utils;

import com.alibaba.fastjson.JSON;

/**
 * @author chenjiawei
 * @date 2018-03-28
 */
public class JsonSizeCounter implements SizeCounter {
    @Override
    public long calculateSize(Object o) {
        return JSON.toJSONString(o).getBytes().length;
    }
}
