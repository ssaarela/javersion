package org.javersion.util;

import java.util.Map;
import java.util.Map.Entry;

public interface MutableMap<K, V> extends Map<K, V>, Iterable<Entry<K, V>> {

    void merge(K key, V value, Merger<Map.Entry<K, V>> merger);

    void mergeAll(Map<? extends K, ? extends V> map, Merger<Entry<K, V>> merger);

    void mergeAll(Iterable<Entry<K, V>> entries, Merger<Entry<K, V>> merger);

    PersistentMap<K, V> toPersistentMap();

}