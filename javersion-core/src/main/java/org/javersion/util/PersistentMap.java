package org.javersion.util;

import java.util.Collection;
import java.util.Map;


public class PersistentMap<K, V> extends AbstractTrieMap<K, V, PersistentMap<K, V>> {

    protected static final class UpdateContext<K, V> implements Merger<K, V> {
        
        private final Thread owner = Thread.currentThread();
        
        final int expectedUpdates;
        
        Merger<K, V> merger;
        
        private int change = 0;
        
        private UpdateContext(int expectedUpdates) {
            this(expectedUpdates, null);
        }
        private UpdateContext(int expectedUpdates, Merger<K, V> merger) {
            this.expectedUpdates = expectedUpdates;
            this.merger = merger;
        }
        
        int getChangeAndReset() {
            try {
                return change;
            } finally {
                change = 0;
            }
        }

        @Override
        public void insert(Entry<K, V> newEntry) {
            change = 1;
            if (merger != null) {
                merger.insert(newEntry);
            }
        }

        @Override
        public Entry<K, V> merge(Entry<K, V> oldEntry, Entry<K, V> newEntry) {
            return merger == null ? newEntry : merger.merge(oldEntry, newEntry);
        }
        
        @Override
        public void delete(Entry<K, V> oldEntry) {
            change = -1;
            if (merger != null) {
                merger.delete(oldEntry);
            }
        }
        
        public void validate() {
            if (owner != Thread.currentThread()) {
                throw new IllegalStateException("MutableMap should only be accessed form the thread it was created in.");
            }
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
    protected ContextHolder<K, V> updateContext(int expectedUpdates, Merger<K, V> merger) {
        return new ContextHolder<K, V>(new UpdateContext<K, V>(expectedUpdates, merger));
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
        return update(expectedUpdates, updateFunction, null);
    }

    public PersistentMap<K, V> update(int expectedUpdates, MapUpdate<K, V> updateFunction, Merger<K, V> merger) {
        ContextHolder<K, V> context = updateContext(expectedUpdates, merger);
        MutableMap<K, V> mutableMap = new MutableMap<>(context, root, size);
        updateFunction.apply(mutableMap);
        return doReturn(context, mutableMap.getRoot(), mutableMap.size());
    }

    @Override
    protected PersistentMap<K, V> doReturn(ContextHolder<K, V> context, Node<K, V> newRoot, int newSize) {
        context.commit();
        if (newRoot == root) {
            return this;
        } else {
            return create(newRoot, newSize);
        }
    }
    
}
