package org.flywaydb.test.util;

import java.util.*;

//TODO investigate erasure problem, why it is not possible to extend from TreeMap?
public class SortedSetMultiMap<K, V> {
    private SortedMap<K, Set<V>> internalMap = new TreeMap<K, Set<V>>();

    public Comparator<? super K> comparator() {
        return internalMap.comparator();
    }

    public SortedMap<K, Set<V>> subMap(K fromKey, K toKey) {
        return internalMap.subMap(fromKey, toKey);
    }

    public SortedMap<K, Set<V>> headMap(K toKey) {
        return internalMap.headMap(toKey);
    }

    public SortedMap<K, Set<V>> tailMap(K fromKey) {
        return internalMap.tailMap(fromKey);
    }

    public K firstKey() {
        return internalMap.firstKey();
    }

    public K lastKey() {
        return internalMap.lastKey();
    }

    public int size() {
        return internalMap.size();
    }

    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    public Set<V> get(Object key) {
        Set<V> value = internalMap.get(key);
        return value == null ? Collections.<V>emptySet() : value;
    }

    public Set<V> put(K key, Set<V> value) {
        Set<V> previousValue = get(key);
        previousValue.addAll(value);
        return previousValue;
    }

    public Set<V> put(K key, V value) {
        Set<V> previousValue = get(key);
        previousValue.add(value);
        return previousValue;
    }

    public Set<V> remove(Object key) {
        return internalMap.remove(key);
    }

    public void putAll(Map<? extends K, ? extends Set<V>> m) {
        internalMap.putAll(m);
    }

    public void clear() {
        internalMap.clear();
    }

    public Set<K> keySet() {
        return internalMap.keySet();
    }

    public Collection<Set<V>> values() {
        return internalMap.values();
    }

    public Set<Map.Entry<K, Set<V>>> entrySet() {
        return internalMap.entrySet();
    }
}
