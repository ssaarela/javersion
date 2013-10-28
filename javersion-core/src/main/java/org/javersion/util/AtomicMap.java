package org.javersion.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.javersion.util.CompareAndSet.AtomicFunction;
import org.javersion.util.CompareAndSet.AtomicVoidFunction;
import org.javersion.util.CompareAndSet.Result;

public class AtomicMap<K, V> extends AbstractMap<K, V> {
    
    private CompareAndSet<PersistentMap<K, V>> atomicMap;
    
    public AtomicMap() {
        this(new PersistentMap<K, V>());
    }
    
    public AtomicMap(PersistentMap<K, V> map) {
        this.atomicMap = new CompareAndSet<>(Check.notNull(map, "map"));
    }
    
    public PersistentMap<K, V> getPersistentMap() {
        return atomicMap.get();
    }

    @Override
    public int size() {
        return atomicMap.get().size;
    }

    @Override
    public boolean containsKey(Object key) {
        return atomicMap.get().containsKey(key);
    }

    @Override
    public V get(Object key) {
        return atomicMap.get().get(key);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final PersistentMap<K, V> map = atomicMap.get();
        return new AbstractSet<Map.Entry<K, V>>() {

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return map.iterator();
            }

            @Override
            public int size() {
                return map.size;
            }
        };
    }

    @Override
    public V put(final K key, final V value) {
        return atomicMap.invoke(new AtomicFunction<PersistentMap<K,V>, V>() {
            @Override
            public PersistentMap<K, V> invoke(PersistentMap<K, V> map, Result<V> result) {
                result.set(map.get(key));
                return map.assoc(key, value);
            }
        });
    }

    @Override
    public V remove(final Object key) {
        return atomicMap.invoke(new AtomicFunction<PersistentMap<K,V>, V>() {
            @Override
            public PersistentMap<K, V> invoke(PersistentMap<K, V> map, Result<V> result) {
                result.set(map.get(key));
                return map.dissoc(key);
            }
        });
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        atomicMap.invoke(new AtomicVoidFunction<PersistentMap<K,V>>() {
            @Override
            public PersistentMap<K, V> invoke(PersistentMap<K, V> map) {
                return map.assocAll(m);
            }
        });
    }

    @Override
    public void clear() {
        atomicMap.invoke(new AtomicVoidFunction<PersistentMap<K,V>>() {
            @Override
            public PersistentMap<K, V> invoke(PersistentMap<K, V> map) {
                PersistentMap.Builder<K, V> builder = PersistentMap.builder(map);
                for (Map.Entry<K, V> entry : map) {
                    builder.remove(entry.getKey());
                }
                return builder.build();
            }
        });
    }
    
}