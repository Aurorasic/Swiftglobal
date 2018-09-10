package com.higgsblock.global.chain.app.keyvalue.core;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
public class IndexedKeyValueTemplate extends KeyValueTemplate {

    private IndexedKeyValueAdapter adapter;
    private KeyValueMappingContext mappingContext;

    public IndexedKeyValueTemplate(IndexedKeyValueAdapter adapter, KeyValueMappingContext mappingContext) {
        super(adapter, mappingContext);
        this.adapter = adapter;
        this.mappingContext = mappingContext;
    }

    @Override
    public void insert(Serializable id, Object objectToInsert) {
        super.insert(id, objectToInsert);
        addIndex(id, objectToInsert);
    }

    @Override
    public void update(Serializable id, Object objectToUpdate) {
        super.update(id, objectToUpdate);
        addIndex(id, objectToUpdate);
    }

    @Override
    public void delete(Class<?> type) {
        String keyspace = this.mappingContext.getPersistentEntity(type).getKeySpace();
        Iterable<?> objects = adapter.getAllOf(keyspace);
        CollectionUtils.forAllDo(Lists.newLinkedList(objects), this::delete);
    }

    @Override
    public <T> T delete(Serializable id, Class<T> type) {
        T object = super.delete(id, type);
        deleteIndex(id, object);
        return object;
    }

    protected void deleteIndex(Serializable id, Object object) {
        if (null == object) {
            return;
        }
        Class<?> clazz = object.getClass();
        String keyspace = this.mappingContext.getPersistentEntity(clazz).getKeySpace();

        EntityClassInfos.getIndexFields(clazz).forEach(field -> {
            Object index = ReflectionUtils.getField(field, object);
            adapter.deleteIndex(field.getName(), (Serializable) index, id, keyspace);
        });
    }

    protected void addIndex(Serializable id, Object objectToUpdate) {
        Class<?> clazz = objectToUpdate.getClass();
        String keyspace = this.mappingContext.getPersistentEntity(clazz).getKeySpace();

        EntityClassInfos.getIndexFields(clazz).forEach(field -> {
            Object index = ReflectionUtils.getField(field, objectToUpdate);
            adapter.addIndex(field.getName(), (Serializable) index, id, keyspace);
        });
    }
}
