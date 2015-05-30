package org.javersion.store.jdbc;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.javersion.core.Revision;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class VersionGraphCache<Id, M> {

    private final LoadingCache<Id, ObjectVersionGraph<M>> cache;

    // About CacheBuilder generics: https://code.google.com/p/guava-libraries/issues/detail?id=738
    public VersionGraphCache(ObjectVersionStoreJdbc<Id, M> versionStore, CacheBuilder<Object, Object> cacheBuilder) {
        cache = cacheBuilder.build(new CacheLoader<Id, ObjectVersionGraph<M>>() {

                    @Override
                    public ObjectVersionGraph<M> load(Id docId) throws Exception {
                        return versionStore.load(docId);
                    }

                    @Override
                    public ListenableFuture<ObjectVersionGraph<M>> reload(Id docId, ObjectVersionGraph<M> oldValue) throws Exception {
                        Revision since = null;
                        if (!oldValue.isEmpty()) {
                            since = oldValue.getTip().getRevision();
                        }
                        ObjectVersionGraph<M> newValue = oldValue;
                        List<ObjectVersion<M>> updates = versionStore.fetchUpdates(docId, since);
                        if (!updates.isEmpty()) {
                            newValue = oldValue.commit(updates);
                        }
                        return Futures.immediateFuture(newValue);
                    }

                });
    }

    public ObjectVersionGraph<M> load(Id docId) {
        try {
            return cache.get(docId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void refresh(Id docId) {
        cache.refresh(docId);
    }
}
