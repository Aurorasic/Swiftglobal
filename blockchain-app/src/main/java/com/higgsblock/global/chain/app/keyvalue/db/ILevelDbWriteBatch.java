package com.higgsblock.global.chain.app.keyvalue.db;

import org.iq80.leveldb.WriteBatch;

import java.io.Closeable;
import java.io.Serializable;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public interface ILevelDbWriteBatch extends Closeable {

    ILevelDbWriteBatch put(Serializable key, Serializable value);

    ILevelDbWriteBatch delete(Serializable key);

    boolean isDeleted(Serializable key);

    WriteBatch wrapper(WriteBatch writeBatch);
}
