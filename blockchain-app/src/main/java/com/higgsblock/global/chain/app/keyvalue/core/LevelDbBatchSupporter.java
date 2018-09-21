package com.higgsblock.global.chain.app.keyvalue.core;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.keyvalue.db.ILevelDb;
import com.higgsblock.global.chain.app.keyvalue.db.ILevelDbWriteBatch;
import com.higgsblock.global.chain.app.keyvalue.db.LevelDbWriteBatch;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.WriteOptions;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author baizhengwen
 * @date 2018-08-24
 */
@Slf4j
public class LevelDbBatchSupporter {

    private static final String KEYSPACE_ENTITY_CLASS = "_EC";
    private static final String KEYSPACE_BATCH = "_batch";
    private static final String KEYSPACE_BATCH_NO = "_batchNos";
    private static final String ID_BATCH_NO = "batchNos";

    @Setter
    protected ReadOptions readOptions;
    @Setter
    protected WriteOptions writeOptions;
    @Setter
    protected ILevelDb<String> db;

    private Map<Serializable, Class> classMap = Maps.newConcurrentMap();
    private Map<String, ILevelDbWriteBatch> writeBatchMap = Maps.newConcurrentMap();
    private String startBatchNo;
    private AtomicLong batchNoCounter;

    public LevelDbBatchSupporter(ILevelDb<String> db) {
        this.db = db;
        readOptions = new ReadOptions();
        writeOptions = new WriteOptions();
        startBatchNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("_yyyyMMddHHmmss@"));
        batchNoCounter = new AtomicLong();
    }

    public ILevelDbWriteBatch createWriteBatch() {
        return writeBatchMap.computeIfAbsent(newBatchNo(), key -> new LevelDbWriteBatch(key));
    }

    public ILevelDbWriteBatch removeWriteBatch(String batchNo) {
        return writeBatchMap.remove(batchNo);
    }

    public void writeWriteBatch(ILevelDbWriteBatch batch) {
        put(batch.getBatchNo(), batch, KEYSPACE_BATCH);
    }

    public void deleteWriteBatch(String batchNo) {
        writeBatchMap.remove(batchNo);
        delete(batchNo, KEYSPACE_BATCH);
    }

    public ILevelDbWriteBatch getWriteBatch(String batchNo) {
        return writeBatchMap.computeIfAbsent(batchNo, key -> (ILevelDbWriteBatch) get(batchNo, KEYSPACE_BATCH));
    }

    public void putBatchNoList(List<String> batchNoList) {
        put(ID_BATCH_NO, batchNoList, KEYSPACE_BATCH_NO);
    }

    public void addBatchNo(String batchNo) {
        List<String> list = getBatchNoList();
        list.add(batchNo);
        putBatchNoList(list);
    }

    public void deleteBatchNo(List<String> batchNoList) {
        List<String> list = getBatchNoList();
        list.removeAll(batchNoList);
        putBatchNoList(list);
    }

    public List<String> getBatchNoList() {
        List<String> list = (List<String>) get(ID_BATCH_NO, KEYSPACE_BATCH_NO);
        if (null == list) {
            list = Lists.newLinkedList();
        }
        return list;
    }

    public void clear() {
        classMap.clear();
        writeBatchMap.clear();
    }

    public void close() {
        try {
            db.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Object put(Serializable id, Object item, Serializable keyspace) {
        if (null == item) {
            delete(id, keyspace);
        }
        putEntityClass(keyspace, item.getClass());
        String key = KeyValueAdapterUtils.getInternalKey(keyspace, id);
        String value = JSON.toJSONString(item);
        db.put(key, value, writeOptions);
        return item;
    }

    private Object get(Serializable id, Serializable keyspace) {
        String key = KeyValueAdapterUtils.getInternalKey(keyspace, id);
        String value = db.get(key, readOptions);
        return KeyValueAdapterUtils.parseJsonString(value, getEntityClass(keyspace));
    }

    private Object delete(Serializable id, Serializable keyspace) {
        Object value = get(id, keyspace);
        String key = KeyValueAdapterUtils.getInternalKey(keyspace, id);
        db.delete(key, writeOptions);
        return value;
    }

    private void putEntityClass(Serializable keyspace, Class<?> clazz) {
        if (null == getEntityClass(keyspace)) {
            addEntityClass(keyspace, clazz);
        }
        classMap.putIfAbsent(keyspace, clazz);
    }

    private void addEntityClass(Serializable keyspace, Class<?> clazz) {
        String key = KeyValueAdapterUtils.getInternalKey(KEYSPACE_ENTITY_CLASS, keyspace);
        db.put(key, clazz.getName(), writeOptions);
    }

    private Class<?> getEntityClass(Serializable keyspace) {
        Class<?> clazz = classMap.get(keyspace);
        if (null != clazz) {
            return clazz;
        }
        String key = KeyValueAdapterUtils.getInternalKey(KEYSPACE_ENTITY_CLASS, keyspace);
        String className = db.get(key, readOptions);
        try {
            clazz = StringUtils.isEmpty(className) ? null : Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return clazz;
    }

    private String newBatchNo() {
        return startBatchNo + batchNoCounter.incrementAndGet();
    }
}
