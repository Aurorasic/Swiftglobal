package com.higgsblock.global.chain.app.keyvalue.db;

import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.WriteBatch;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public class LevelDbWriteBatch implements ILevelDbWriteBatch {

    private final List<Map.Entry<Serializable, Serializable>> batch = Collections.synchronizedList(Lists.newLinkedList());

    public LevelDbWriteBatch() {
    }

    @Override
    public ILevelDbWriteBatch put(Serializable key, Serializable value) {
        batch.add(Maps.immutableEntry(key, value));
        return this;
    }

    @Override
    public ILevelDbWriteBatch delete(Serializable key) {
        batch.add(Maps.immutableEntry(key, null));
        return this;
    }

    @Override
    public boolean isDeleted(Serializable key) {
        boolean isDeleted = false;
        for (Map.Entry<Serializable, Serializable> entry : batch) {
            if (Equivalence.equals().equivalent(key, entry.getKey()) && null == entry.getValue()) {
                isDeleted = true;
            } else if (Equivalence.equals().equivalent(key, entry.getKey()) && null != entry.getValue() && isDeleted) {
                isDeleted = false;
            }
        }
        return isDeleted;
    }

    @Override
    public WriteBatch wrapper(WriteBatch writeBatch) {
        batch.forEach(entry -> {
            byte[] key = SerializationUtils.serialize(entry.getKey());
            byte[] value = SerializationUtils.serialize(entry.getValue());
            if (null == value) {
                writeBatch.delete(key);
            } else {
                writeBatch.put(key, value);
            }
        });
        return writeBatch;
    }

    @Override
    public void close() {

    }
}
