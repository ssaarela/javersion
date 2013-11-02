package org.javersion.util;


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
    public static final PersistentMap EMPTY = new PersistentMap(null, 0);
    
    private final Node<K, V> root;
    
    private final int size;

    public PersistentMap() {
        this(null, 0);
    }
    
    @SuppressWarnings("unchecked")
    PersistentMap(Node<? extends K, ? extends V> root, int size) {
        this.root = (Node<K, V>) root == null ? HashNode.EMPTY: root;
        this.size = size;
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
            return new PersistentMap<K, V>(newRoot, newSize);
        }
    }
    
}
