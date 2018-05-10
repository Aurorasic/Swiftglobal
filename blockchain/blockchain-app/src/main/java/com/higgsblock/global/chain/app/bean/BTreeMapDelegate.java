package com.higgsblock.global.chain.app.bean;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @param <K> byte[]
 * @param <V> byte[]
 * @author kongyu
 * @date 2018-03-23
 */
@Slf4j
public class BTreeMapDelegate<K, V> implements ConcurrentMap<K, V> {
    private DB db;
    private BTreeMap<K, V> map;

    public BTreeMapDelegate(DB db, BTreeMap<K, V> map) {
        Preconditions.checkNotNull(map, "db can not be null");
        Preconditions.checkNotNull(map, "map can not be null");
        this.db = db;
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        if (key == null) {
            return null;
        }
        return map.get(key);
    }

//    public ConcurrentNavigableMap<byte[], byte[]> prefixSubMap(byte[] pubKey) {
//        return (ConcurrentNavigableMap<byte[], byte[]>) this.map.prefixSubMap(pubKey);
//    }
//
//    public ConcurrentNavigableMap<byte[], byte[]> prefixSubMap(byte[] pubKey, boolean inclusive) {
//        return (ConcurrentNavigableMap<byte[], byte[]>) this.map.prefixSubMap(pubKey, inclusive);
//    }

    @Override
    public V put(K key, V value) {
        return doTransaction(() -> map.put(key, value));
    }

    @Override
    public V remove(Object key) {
        return doTransaction(() -> map.remove(key));
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        doTransaction(() -> map.putAll(m));
    }

    @Override
    public void clear() {
        doTransaction(() -> map.clear());
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return doTransaction(() -> map.getOrDefault(key, defaultValue));
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    @Override
    public V putIfAbsent(@NotNull K key, V value) {
        return doTransaction(() -> map.putIfAbsent(key, value));
    }

    @Override
    public boolean remove(@NotNull Object key, Object value) {
        return doTransaction(() -> map.remove(key, value));
    }

    @Override
    public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
        return doTransaction(() -> map.replace(key, oldValue, newValue));
    }

    @Override
    public V replace(@NotNull K key, @NotNull V value) {
        return doTransaction(() -> map.replace(key, value));
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        doTransaction(() -> map.replaceAll(function));
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return doTransaction(() -> map.computeIfAbsent(key, mappingFunction));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return doTransaction(() -> map.computeIfPresent(key, remappingFunction));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return doTransaction(() -> map.compute(key, remappingFunction));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return doTransaction(() -> map.merge(key, value, remappingFunction));
    }

    private <T> T doTransaction(Callable<T> callable) {
        T call = null;
        try {
            call = callable.call();
            db.commit();
        } catch (Exception e) {
            db.rollback();
            LOGGER.error(e.getMessage(), e);
        }
        return call;
    }

    private void doTransaction(Runnable runnable) {
        try {
            runnable.run();
            db.commit();
        } catch (Exception e) {
            db.rollback();
            LOGGER.error(e.getMessage(), e);
        }
    }
}
