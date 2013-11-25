package org.javersion.util;

import java.util.Map;


public class PersistentHashMap<K, V> extends AbstractHashMap<K, V, PersistentHashMap<K, V>> {
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final PersistentHashMap EMPTY_MAP = new PersistentHashMap(EMPTY_NODE, 0);
    
    private final Node<K, Entry<K, V>> root;
    
    private final int size;

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentHashMap<K, V> empty() {
        return (PersistentHashMap<K, V>) EMPTY_MAP;
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentHashMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return ((PersistentHashMap<K, V>) EMPTY_MAP).assocAll(map);
    }
    
    public static <K, V> PersistentHashMap<K, V> of() {
        return empty();
    }
    
    public static <K, V> PersistentHashMap<K, V> of(K k1, V v1) {
        return new MutableHashMap<K, V>()
                .assoc(k1, v1)
                .toPersistentMap();
    }
    
    public static <K, V> PersistentHashMap<K, V> of(K k1, V v1, K k2, V v2) {
        return new MutableHashMap<K, V>()
                .assoc(k1, v1)
                .assoc(k2, v2)
                .toPersistentMap();
    }
    
    public static <K, V> PersistentHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return new MutableHashMap<K, V>()
                .assoc(k1, v1)
                .assoc(k2, v2)
                .assoc(k3, v3)
                .toPersistentMap();
    }

    
    @SuppressWarnings("unchecked")
    static <K, V> PersistentHashMap<K, V> create(Node<K, Entry<K, V>> newRoot, int newSize) {
        return newRoot == null ? (PersistentHashMap<K, V>) EMPTY_MAP : new PersistentHashMap<K, V>(newRoot, newSize);
    }
    
    private PersistentHashMap(Node<K, Entry<K, V>> newRoot, int newSize) {
        this.root = newRoot;
        this.size = newSize;
    }
    
    public MutableHashMap<K, V> toMutableMap() {
        return new MutableHashMap<K, V>(root, size);
    }
    
    public Map<K, V> asMap() {
        return new ImmutableTrieMap<>(this);
    }

    @Override
    protected PersistentHashMap<K, V> self() {
        return this;
    }

    @Override
    protected Node<K, Entry<K, V>> root() {
        return root;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public PersistentHashMap<K, V> update(int expectedUpdates, MapUpdate<K, V> updateFunction, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(expectedUpdates, merger);
        try {
            MutableHashMap<K, V> mutableMap = new MutableHashMap<>(context, root, size);
            updateFunction.apply(mutableMap);
            return doReturn(mutableMap.root(), mutableMap.size());
        } finally {
            context.commit();
        }
    }

    @Override
    protected PersistentHashMap<K, V> doReturn(Node<K, Entry<K, V>> newRoot, int newSize) {
        if (newRoot == root) {
            return this;
        } else {
            return create(newRoot, newSize);
        }
    }
    
}
