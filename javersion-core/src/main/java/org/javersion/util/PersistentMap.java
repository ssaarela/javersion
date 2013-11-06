package org.javersion.util;

import java.util.Collection;
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
    
    public static <K, V> PersistentMap<K, V> copyOf(Collection<Map.Entry<? extends K, ? extends V>> entries) {
        return copyOf(entries, entries.size());
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentMap<K, V> copyOf(Iterable<Map.Entry<? extends K, ? extends V>> entries, int expectedUpdates) {
        return ((PersistentMap<K, V>) EMPTY_MAP).assocAll(entries, expectedUpdates);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return ((PersistentMap<K, V>) EMPTY_MAP).assocAll(map);
    }
    
    @SuppressWarnings("unchecked")
    private static <K, V> PersistentMap<K, V> create(Node<? extends K, ? extends V> newRoot, int newSize) {
        return newRoot == null ? (PersistentMap<K, V>) EMPTY_MAP : new PersistentMap<K, V>(newRoot, newSize);
    }
    
    @SuppressWarnings("unchecked")
    PersistentMap(Node<? extends K, ? extends V> newRoot, int newSize) {
        this.root = (Node<K, V>) newRoot;
        this.size = newSize;
    }
    
    public MutableMap<K, V> toMutableMap() {
        return new MutableMap<K, V>(root, size);
    }
    
    public ImmutableMap<K, V> asImmutableMap() {
        return new ImmutableMap<>(this);
    }

    @Override
    protected PersistentMap<K, V> self() {
        return this;
    }

    @Override
    protected ContextReference<K, V> contextReference(int expectedUpdates, Merger<K, V> merger) {
        return new ContextReference<K, V>(new UpdateContext<K, V>(expectedUpdates, merger));
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
    public PersistentMap<K, V> update(int expectedUpdates, MapUpdate<K, V> updateFunction, Merger<K, V> merger) {
        ContextReference<K, V> context = contextReference(expectedUpdates, merger);
        MutableMap<K, V> mutableMap = new MutableMap<>(context, root, size);
        updateFunction.apply(mutableMap);
        return doReturn(context, mutableMap.getRoot(), mutableMap.size());
    }

    @Override
    protected PersistentMap<K, V> doReturn(ContextReference<K, V> context, Node<K, V> newRoot, int newSize) {
        context.commit();
        if (newRoot == root) {
            return this;
        } else {
            return create(newRoot, newSize);
        }
    }
    
}
