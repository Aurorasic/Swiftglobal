package com.higgsblock.global.chain.app.keyvalue.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.higgsblock.global.chain.app.keyvalue.annotation.Index;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author baizhengwen
 * @date 2018-09-06
 */
public abstract class EntityClassInfos {

    private static final Map<Class, Set<Field>> fieldMap = Maps.newConcurrentMap();

    public static Collection<Field> getFields(Class clazz) {
        return fieldMap.computeIfAbsent(clazz, aClass -> {
            Set<Field> fields = Sets.newHashSet();
            ReflectionUtils.doWithLocalFields(clazz, field -> {
                field.setAccessible(true);
                fields.add(field);
            });
            return fields;
        });
    }

    public static Collection<Field> getIndexFields(Class clazz) {
        return getFields(clazz).stream()
                .filter(field -> null != field.getAnnotation(Index.class))
                .collect(Collectors.toList());
    }

    public static Field getIdFields(Class clazz) {
        return getFields(clazz).stream()
                .filter(field -> null != field.getAnnotation(Id.class))
                .findFirst()
                .orElse(null);
    }

    public static boolean isIndex(Class clazz, String fieldName) {
        return getIndexFields(clazz).stream().anyMatch(field -> null != field && StringUtils.equals(fieldName, field.getName()));
    }

    public static boolean isId(Class clazz, String fieldName) {
        Field idField = getIdFields(clazz);
        return null != idField && StringUtils.equals(fieldName, idField.getName());
    }
}
