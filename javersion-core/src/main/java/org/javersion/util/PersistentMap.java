package org.javersion.util;

import java.util.Collection;
import java.util.Map;


public class PersistentMap<K, V> extends AbstractTrieMap<K, V, PersistentMap<K, V>> {

    protected static final class UpdateContext {
        
        final int expectedUpdates;
        
        private int change = 0;
        
        private UpdateContext(int expectedUpdates) {
            this.expectedUpdates = expectedUpdates;
        }
        
        int getChangeAndReset() {
            try {
                return change;
            } finally {
                change = 0;
            }
        }
        
        void recordAddition() {
            change = 1;
        }
        
        void recordRemoval() {
            change = -1;
        }
    }
    
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
    private PersistentMap(Node<? extends K, ? extends V> newRoot, int newSize) {
        this.root = (Node<K, V>) newRoot;
        this.size = newSize;
    }
    
    public ImmutableMap<K, V> toImmutableMap() {
        return new ImmutableMap<>(this);
    }
    
    public AtomicMap<K, V> toAtomicMap() {
        return new AtomicMap<>(this);
    }

    @Override
    protected PersistentMap<K, V> self() {
        return this;
    }

    @Override
    protected UpdateContext updateContext(int expectedUpdates) {
        return new UpdateContext(expectedUpdates);
    }

    @Override
    protected Node<K, V> getRoot() {
        return root;
    }

    @Override
    public int size() {
        return size;
    }

    public PersistentMap<K, V> update(MapUpdate<K, V> updateFunction) {
        return update(32, updateFunction);
    }

    public PersistentMap<K, V> update(int expectedUpdates, MapUpdate<K, V> updateFunction) {
        MutableMap<K, V> mutableMap = new MutableMap<>(new UpdateContext(expectedUpdates), root, size);
        updateFunction.apply(mutableMap);
        return doReturn(mutableMap.getRoot(), mutableMap.size());
    }

    @Override
    protected PersistentMap<K, V> doReturn(Node<? extends K, ? extends V> newRoot, int newSize) {
        if (newRoot == root) {
            return this;
        } else {
            return create(newRoot, newSize);
        }
    }
    
}
