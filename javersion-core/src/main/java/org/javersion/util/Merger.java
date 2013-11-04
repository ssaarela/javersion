package org.javersion.util;

import org.javersion.util.AbstractTrieMap.Entry;

public interface Merger<K, V> {

    public void insert(Entry<K, V> newEntry);
    
    public Entry<K, V> merge(Entry<K, V> oldEntry, Entry<K, V> newEntry);

    public void delete(Entry<K, V> oldEntry);
    
}
