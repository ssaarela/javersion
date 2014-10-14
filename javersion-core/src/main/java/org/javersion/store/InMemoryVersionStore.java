package org.javersion.store;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.javersion.core.Version;

public class InMemoryVersionStore<I, K, V> implements VersionStore<I, K, V> {

    private static class Entity<K, V> {
        private long firstRevision;
        private long lastRevision;
        private final TreeMap<Long, Version<K, V>> versions = new TreeMap<>();
    }

    private final ConcurrentHashMap<I, Entity> entities = new ConcurrentHashMap<>();

    @Override
    public void append(I id, Version<K, V> version) {
        Entity<K, V> entity = entities.computeIfAbsent(id, new Function<I, Entity<K, V>>() {
            @Override
            public Entity<K, V> apply(I i) {
                return new Entity<K, V>();
            }
        });
        synchronized (entity) {

        }
    }

    @Override
    public void append(I id, Iterable<Version<K, V>> versions) {

    }

    @Override
    public Iterable<Version<K, V>> load(I id) {
        return null;
    }

    @Override
    public Iterable<Version<K, V>> load(I id, @Nullable Long sinceRevision, @Nullable Long untilRevision) {
        return null;
    }
}
