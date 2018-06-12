package com.higgsblock.global.chain.app.utils;

import com.google.common.base.Objects;

import java.util.*;

/**
 * Map sorted by value.
 *
 *  * @author zhao xiaogang
 *  * @date 2018-03-28
 */
public class ValueSortedMap<K, V> extends HashMap<K, V> {
    protected Map<V, Collection<K>> valueToKeysMap;

    public ValueSortedMap() {
        this(null);
    }

    public ValueSortedMap(Comparator<? super V> valueComparator) {
        this.valueToKeysMap = new TreeMap<>(valueComparator);
    }

    @Override
    public synchronized boolean containsValue(Object o) {
        return valueToKeysMap.containsKey(o);
    }

    @Override
    public synchronized V put(K k, V v) {
        V oldV = null;
        if (containsKey(k)) {
            oldV = get(k);
            valueToKeysMap.get(oldV).remove(k);
        }
        super.put(k, v);
        if (!valueToKeysMap.containsKey(v)) {
            Collection<K> keys = new ArrayList<>();
            keys.add(k);
            valueToKeysMap.put(v, keys);
        } else {
            valueToKeysMap.get(v).add(k);
        }
        return oldV;
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()){
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public synchronized V remove(Object k) {
        V oldV = null;
        if (containsKey(k)) {
            oldV = get(k);
            super.remove(k);
            valueToKeysMap.get(oldV).remove(k);
        }
        return oldV;
    }

    @Override
    public synchronized void clear() {
        super.clear();
        valueToKeysMap.clear();
    }

    @Override
    public synchronized int size() {
        return super.size();
    }

    @Override
    public synchronized Set<K> keySet() {
        LinkedHashSet<K> ret = new LinkedHashSet<K>(size());
        for (V v : valueToKeysMap.keySet()) {
            Collection<K> keys = valueToKeysMap.get(v);
            ret.addAll(keys);
        }
        return ret;
    }

    @Override
    public synchronized Set<Entry<K, V>> entrySet() {
        LinkedHashSet<Entry<K, V>> ret = new LinkedHashSet<>(size());
        for (Collection<K> keys : valueToKeysMap.values()) {
            for (final K k : keys) {
                final V v = get(k);
                ret.add(new Entry<K,V>() {
                    @Override
                    public K getKey() {
                        return k;
                    }
                    @Override
                    public V getValue() {
                        return v;
                    }
                    @Override
                    public V setValue(V v) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public int hashCode() {
                        return  (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
                    }

                    @Override
                    public boolean equals(Object object) {
                        if (!(object instanceof Entry)) {
                            return false;
                        } else {
                            Entry<?, ?> that = (Entry)object;
                            return com.google.common.base.Objects.equal(this.getKey(),
                                    that.getKey()) && Objects.equal(this.getValue(), that.getValue());
                        }
                    }
                });
            }
        }
        return ret;
    }

    @Override
    public synchronized Collection<V> values() {
        return super.values();
    }
}