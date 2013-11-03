package org.javersion.util;

import org.javersion.util.AbstractTrieMap.Entry;

public interface Merger<K, V> {

    public Entry<K, V> merge(Entry<K, V> oldEntry, Entry<K, V> newEntry);

}
