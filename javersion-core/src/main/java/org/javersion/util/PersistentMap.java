package org.javersion.util;

import java.util.Map;


public class PersistentMap<K, V> extends AbstractTrieMap<K, V, PersistentMap<K, V>> {
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final PersistentMap EMPTY_MAP = new PersistentMap(EMPTY_NODE, 0);
    
    private final Node<K, V> root;
    
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
    static <K, V> PersistentMap<K, V> create(Node<? extends K, ? extends V> newRoot, int newSize) {
        return newRoot == null ? (PersistentMap<K, V>) EMPTY_MAP : new PersistentMap<K, V>(newRoot, newSize);
    }
    
    @SuppressWarnings("unchecked")
    private PersistentMap(Node<? extends K, ? extends V> newRoot, int newSize) {
        this.root = (Node<K, V>) newRoot;
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
    protected ContextReference<Entry<K, V>> contextReference(int expectedUpdates, Merger<Entry<K, V>> merger) {
        return new ContextReference<Entry<K, V>>(new UpdateContext<Entry<K, V>>(expectedUpdates, merger));
    }

    @Override
    Node<K, V> getRoot() {
        return root;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public PersistentMap<K, V> update(int expectedUpdates, MapUpdate<K, V> updateFunction, Merger<Entry<K, V>> merger) {
        ContextReference<Entry<K, V>> context = contextReference(expectedUpdates, merger);
        MutableMap<K, V> mutableMap = new MutableMap<>(context, root, size);
        updateFunction.apply(mutableMap);
        return doReturn(context, mutableMap.getRoot(), mutableMap.size());
    }

    @Override
    protected PersistentMap<K, V> doReturn(ContextReference<Entry<K, V>> context, Node<K, V> newRoot, int newSize) {
        context.commit();
        if (newRoot == root) {
            return this;
        } else {
            return create(newRoot, newSize);
        }
    }
    
}
