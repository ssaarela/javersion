package org.javersion.util;

import java.util.Map;


public class PersistentMap<K, V> extends AbstractTrieMap<K, V, PersistentMap<K, V>> {
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final PersistentMap EMPTY_MAP = new PersistentMap(EMPTY_NODE, 0);
    
    private final Node<K, Entry<K, V>> root;
    
    private final int size;

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentMap<K, V> empty() {
        return (PersistentMap<K, V>) EMPTY_MAP;
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return ((PersistentMap<K, V>) EMPTY_MAP).assocAll(map);
    }
    
    public static <K, V> PersistentMap<K, V> of() {
        return empty();
    }
    
    public static <K, V> PersistentMap<K, V> of(K k1, V v1) {
        return new MutableMap<K, V>()
                .assoc(k1, v1)
                .toPersistentMap();
    }
    
    public static <K, V> PersistentMap<K, V> of(K k1, V v1, K k2, V v2) {
        return new MutableMap<K, V>()
                .assoc(k1, v1)
                .assoc(k2, v2)
                .toPersistentMap();
    }
    
    public static <K, V> PersistentMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return new MutableMap<K, V>()
                .assoc(k1, v1)
                .assoc(k2, v2)
                .assoc(k3, v3)
                .toPersistentMap();
    }

    
    @SuppressWarnings("unchecked")
    static <K, V> PersistentMap<K, V> create(Node<K, Entry<K, V>> newRoot, int newSize) {
        return newRoot == null ? (PersistentMap<K, V>) EMPTY_MAP : new PersistentMap<K, V>(newRoot, newSize);
    }
    
    private PersistentMap(Node<K, Entry<K, V>> newRoot, int newSize) {
        this.root = newRoot;
        this.size = newSize;
    }
    
    public MutableMap<K, V> toMutableMap() {
        return new MutableMap<K, V>(root, size);
    }
    
    public Map<K, V> asMap() {
        return new ImmutableTrieMap<>(this);
    }

    @Override
    protected PersistentMap<K, V> self() {
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
    public PersistentMap<K, V> update(int expectedUpdates, MapUpdate<K, V> updateFunction, Merger<Entry<K, V>> merger) {
        final UpdateContext<Entry<K, V>> context = updateContext(expectedUpdates, merger);
        try {
            MutableMap<K, V> mutableMap = new MutableMap<>(context, root, size);
            updateFunction.apply(mutableMap);
            return doReturn(mutableMap.root(), mutableMap.size());
        } finally {
            context.commit();
        }
    }

    @Override
    protected PersistentMap<K, V> doReturn(Node<K, Entry<K, V>> newRoot, int newSize) {
        if (newRoot == root) {
            return this;
        } else {
            return create(newRoot, newSize);
        }
    }
    
}
