package org.javersion.util;

import org.javersion.util.PersistentMap.AbstractNode;
import org.javersion.util.PersistentMap.CollisionNode;
import org.javersion.util.PersistentMap.Entry;
import org.javersion.util.PersistentMap.HashNode;
import org.javersion.util.PersistentMap.Version;

public final class PersistentMapNodeFactory {

    private PersistentMapNodeFactory() {}

    public static <K, V> Entry<K, V> newEntryNode(int hash, K key, V value) {
        return new Entry<K, V>(hash, key, value);
    }
    
    public static <K, V>  HashNode<K, V> newHashNode(Version version, int bitmap, AbstractNode<K, V>[] children) {
        return new HashNode<>(version, bitmap, children);
    }
    
    public static <K, V>  CollisionNode<K, V> newCollisionNode(Entry<? extends K, ? extends V>[] entries) {
        return new CollisionNode<>(entries);
    }
    
}
