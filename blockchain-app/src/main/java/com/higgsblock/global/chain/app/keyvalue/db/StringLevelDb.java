package com.higgsblock.global.chain.app.keyvalue.db;

import com.google.common.base.Charsets;
import org.apache.commons.lang.ArrayUtils;
import org.iq80.leveldb.Options;

import java.io.Serializable;

/**
 * @author baizhengwen
 * @date 2018-09-14
 */
public class StringLevelDb extends LevelDb<String> {

    public StringLevelDb(String dataPath, Options options) {
        super(dataPath, options);
    }

    @Override
    protected byte[] serialize(Serializable data) {
        return null == data ? new byte[0] : String.valueOf(data).getBytes(Charsets.UTF_8);
    }

    @Override
    protected String deserialize(byte[] bytes) {
        return ArrayUtils.isEmpty(bytes) ? null : new String(bytes, Charsets.UTF_8);
    }
}
