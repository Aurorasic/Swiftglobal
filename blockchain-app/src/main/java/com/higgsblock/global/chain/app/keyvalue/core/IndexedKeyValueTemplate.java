package com.higgsblock.global.chain.app.keyvalue.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.higgsblock.global.chain.app.keyvalue.annotation.Index;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
public class IndexedKeyValueTemplate extends KeyValueTemplate {

    private IndexedKeyValueAdapter adapter;
    private KeyValueMappingContext mappingContext;
    private Map<Class, Set<Field>> fieldMap = Maps.newConcurrentMap();

    public IndexedKeyValueTemplate(IndexedKeyValueAdapter adapter, KeyValueMappingContext mappingContext) {
        super(adapter, mappingContext);
        this.adapter = adapter;
        this.mappingContext = mappingContext;
    }

    @Override
    public void insert(Serializable id, Object objectToInsert) {
        super.insert(id, objectToInsert);
        updateIndex(id, objectToInsert);
    }

    @Override
    public void update(Serializable id, Object objectToUpdate) {
        super.update(id, objectToUpdate);
        updateIndex(id, objectToUpdate);
    }

    protected void updateIndex(Serializable id, Object objectToUpdate) {
        Class<?> clazz = objectToUpdate.getClass();
        String keyspace = this.mappingContext.getPersistentEntity(clazz).getKeySpace();

        getFields(clazz).forEach(field -> {
            Object index = ReflectionUtils.getField(field, objectToUpdate);
            adapter.saveIndex(field.getName(), (Serializable) index, id, keyspace);
        });
    }

    protected Collection<Field> getFields(Class clazz) {
        return fieldMap.computeIfAbsent(clazz, aClass -> {
            Set<Field> fields = Sets.newHashSet();
            ReflectionUtils.doWithLocalFields(clazz, field -> {
                Index index = field.getAnnotation(Index.class);
                if (null != index) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            });
            return fields;
        });
    }
}
