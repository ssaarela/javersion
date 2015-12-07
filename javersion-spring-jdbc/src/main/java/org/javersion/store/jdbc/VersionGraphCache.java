/*
 * Copyright 2015 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javersion.store.jdbc;

import static com.google.common.util.concurrent.Futures.immediateFuture;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.javersion.core.Revision;
import org.javersion.core.VersionGraph;
import org.javersion.core.VersionNotFoundException;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;

public class VersionGraphCache<Id, M> {

    protected final Logger log = LoggerFactory.getLogger(VersionGraph.class);

    protected final LoadingCache<Id, ObjectVersionGraph<M>> cache;

    private final AbstractVersionStoreJdbc<Id, M, ?, ?> versionStore;

    protected final Set<Id> cachedDocIds;


    // About CacheBuilder generics: https://code.google.com/p/guava-libraries/issues/detail?id=738
    public VersionGraphCache(AbstractVersionStoreJdbc<Id, M, ?, ?> versionStore, CacheBuilder<Object, Object> cacheBuilder) {
        this.versionStore = versionStore;

        this.cache = cacheBuilder.build(newCacheLoader(versionStore));
        this.cachedDocIds = cache.asMap().keySet();
    }

    public ObjectVersionGraph<M> load(Id docId) {
        try {
            return cache.get(docId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls wrapped versionStore.publish() and refreshes changed (published)
     * AND cached graphs.
     */
    public Set<Id> publish() {
        Set<Id> publishedDocIds = versionStore.publish().keySet();
        publishedDocIds.forEach(this::refresh);
        return publishedDocIds;
    }

    public void refresh(Id docId) {
        if (cachedDocIds.contains(docId)) {
            cache.refresh(docId);
        }
    }

    public void evict(Id docId) {
        cache.invalidate(docId);
    }

    public void evict(Iterator<Id> docIds) {
        cache.invalidate(docIds);
    }

    public void evictAll() {
        cache.invalidateAll();
    }

    protected CacheLoader<Id, ObjectVersionGraph<M>> newCacheLoader(final AbstractVersionStoreJdbc<Id, M, ?, ?> versionStore) {
        return new CacheLoader<Id, ObjectVersionGraph<M>>() {

            @Override
            public ObjectVersionGraph<M> load(Id docId) throws Exception {
                return versionStore.load(docId);
            }

            @Override
            public ListenableFuture<ObjectVersionGraph<M>> reload(Id docId, ObjectVersionGraph<M> oldValue) throws Exception {
                if (!oldValue.isEmpty()) {
                    ObjectVersionGraph<M> newValue = oldValue;
                    Revision since = oldValue.getTip().getRevision();
                    try {
                        List<ObjectVersion<M>> updates = versionStore.fetchUpdates(docId, since);
                        if (!updates.isEmpty()) {
                            newValue = oldValue.commit(updates);
                        }
                        return immediateFuture(newValue);
                    } catch (VersionNotFoundException e) {
                        // since revision is deleted - reload graph
                    }
                }
                return immediateFuture(versionStore.load(docId));
            }

        };
    }

}
