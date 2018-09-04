package com.higgsblock.global.chain.app.keyvalue.db;

import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.WriteBatch;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public class LevelDbWriteBatch implements ILevelDbWriteBatch {

    private WriteBatch writeBatch;

    public LevelDbWriteBatch(WriteBatch writeBatch) {
        this.writeBatch = writeBatch;
    }

    @Override
    public ILevelDbWriteBatch put(String key, Serializable value) {
        writeBatch.put(SerializationUtils.serialize(key), SerializationUtils.serialize(value));
        return this;
    }

    @Override
    public ILevelDbWriteBatch delete(Serializable key) {
        writeBatch.delete(SerializationUtils.serialize(key));
        return this;
    }

    @Override
    public WriteBatch getWriteBatch() {
        return writeBatch;
    }

    @Override
    public void close() throws IOException {
        writeBatch.close();
    }
}
