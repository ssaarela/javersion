package org.javersion.util;

import java.util.Map;
import java.util.Map.Entry;

public interface PersistentMap<K, V> extends Iterable<Entry<K, V>> {

    PersistentMap<K, V> assoc(K key, V value);

    PersistentMap<K, V> assocAll(Map<? extends K, ? extends V> map);

    PersistentMap<K, V> assocAll(Iterable<Map.Entry<K, V>> entries);

    PersistentMap<K, V> mergeAll(Map<? extends K, ? extends V> map, Merger<Entry<K, V>> merger);

    PersistentMap<K, V> mergeAll(Iterable<Entry<K, V>> entries, Merger<Entry<K, V>> merger);

    PersistentMap<K, V> dissoc(Object key);

    PersistentMap<K, V> dissoc(Object key, Merger<Entry<K, V>> merger);

    V get(Object key);

    boolean containsKey(Object key);

    int size();
    
//    MutableHashMap<K, V> toMutableMap();
    
    Map<K, V> asMap();
}