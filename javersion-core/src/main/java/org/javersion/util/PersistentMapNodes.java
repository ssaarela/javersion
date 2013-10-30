package org.javersion.util;

import org.javersion.util.PersistentMap.Node;
import org.javersion.util.PersistentMap.Version;

public final class PersistentMapNodes {

    private PersistentMapNodes() {}

    public static class Entry<K, V> extends PersistentMap.AbstractEntry<K, V> {

        public Entry(K key, V value) {
            super(key, value);
        }

        protected Entry(int hash, K key, V value) {
            super(hash, key, value);
        }
    }
    
    public static class HashNode<K, V> extends PersistentMap.AbstractHashNode<K, V>{

        public HashNode(Version version) {
            super(version);
        }

        public HashNode(Version version, int expectedSize) {
            super(version, expectedSize);
        }

        protected HashNode(Version version, int bitmap, Node<K, V>[] children) {
            super(version, bitmap, children);
        }
    }
    
    public static class  CollisionNode<K, V> extends PersistentMap.AbstractCollisionNode<K, V> {

        public CollisionNode(
                Entry<? extends K, ? extends V> first,
                Entry<? extends K, ? extends V> second) {
            super(first, second);
        }

        protected CollisionNode(Entry<? extends K, ? extends V>[] entries) {
            super(entries);
        }
    }
    
}
