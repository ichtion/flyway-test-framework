package org.flywaydb.test.util;

import java.util.*;

//TODO investigate erasure problem, why it is not possible to extend from TreeMap?
public class SortedSetMultiMap<K, V> {
    private SortedMap<K, Set<V>> internalMap = new TreeMap<K, Set<V>>();

    public Set<V> get(K key) {
        Set<V> value = internalMap.get(key);
        if (value == null) {
            internalMap.put(key, new HashSet<V>());
        }
        return internalMap.get(key);
    }

    public Set<V> put(K key, V value) {
        Set<V> previousValue = get(key);
        previousValue.add(value);
        return previousValue;
    }

    public Set<K> keySet() {
        return internalMap.keySet();
    }
}
