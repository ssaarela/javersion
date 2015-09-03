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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.javersion.core.Revision;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;

public class VersionGraphCache<Id, M> {

    private final LoadingCache<Id, ObjectVersionGraph<M>> cache;

    private final ObjectVersionStoreJdbc<Id, M> versionStore;

    private final Set<Id> cachedDocIds;


    // About CacheBuilder generics: https://code.google.com/p/guava-libraries/issues/detail?id=738
    public VersionGraphCache(ObjectVersionStoreJdbc<Id, M> versionStore, CacheBuilder<Object, Object> cacheBuilder) {
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

    private CacheLoader<Id, ObjectVersionGraph<M>> newCacheLoader(final ObjectVersionStoreJdbc<Id, M> versionStore) {
        return new CacheLoader<Id, ObjectVersionGraph<M>>() {

            @Override
            public ObjectVersionGraph<M> load(Id docId) throws Exception {
                return versionStore.load(docId);
            }

            @Override
            public ListenableFuture<ObjectVersionGraph<M>> reload(Id docId, ObjectVersionGraph<M> oldValue) throws Exception {
                if (oldValue.isEmpty()) {
                    return immediateFuture(versionStore.load(docId));
                }
                ObjectVersionGraph<M> newValue = oldValue;
                Revision since = oldValue.getTip().getRevision();
                List<ObjectVersion<M>> updates = versionStore.fetchUpdates(docId, since);
                if (!updates.isEmpty()) {
                    newValue = oldValue.commit(updates);
                }
                return immediateFuture(newValue);
            }

        };
    }

}
