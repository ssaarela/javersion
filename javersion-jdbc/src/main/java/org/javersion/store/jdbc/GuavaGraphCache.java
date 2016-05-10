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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import org.javersion.core.Revision;
import org.javersion.core.VersionNotFoundException;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.util.concurrent.Futures.immediateFuture;

public class GuavaGraphCache<Id, M> implements GraphCache<Id, M> {

    public static <Id, M> Function<VersionStore<Id, M>, GraphCache<Id, M>> guavaCacheBuilder(CacheBuilder<Object, Object> cacheBuilder) {
        return guavaCacheBuilder(cacheBuilder, null);
    }

    public static <Id, M> Function<VersionStore<Id, M>, GraphCache<Id, M>> guavaCacheBuilder(CacheBuilder<Object, Object> cacheBuilder,
                                                                                             GraphOptions<Id, M> graphOptions) {
        return versionStore -> new GuavaGraphCache<>(versionStore, cacheBuilder, graphOptions);
    }

    private final Logger log = LoggerFactory.getLogger(GuavaGraphCache.class);

    @SuppressWarnings("unchecked")
    private static final GraphOptions DEFAULT_CACHE_OPTIONS = new GraphOptions();

    protected final LoadingCache<Id, ObjectVersionGraph<M>> cache;

    private final GraphOptions<Id, M> graphOptions;

    protected final Set<Id> cachedDocIds;

    public GuavaGraphCache(VersionStore<Id, M> versionStore,
                           CacheBuilder<Object, Object> cacheBuilder) {
        this(versionStore, cacheBuilder, null);
    }

    // About CacheBuilder generics: https://code.google.com/p/guava-libraries/issues/detail?id=738
    @SuppressWarnings("unchecked")
    public GuavaGraphCache(VersionStore<Id, M> versionStore,
                           CacheBuilder<Object, Object> cacheBuilder,
                           GraphOptions<Id, M> graphOptions) {
        this.cache = cacheBuilder.build(newCacheLoader(versionStore));
        this.cachedDocIds = cache.asMap().keySet();
        this.graphOptions = firstNonNull(graphOptions, DEFAULT_CACHE_OPTIONS);
    }

    @Override
    public ObjectVersionGraph<M> load(Id docId) {
        try {
            return cache.get(docId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refresh(Id docId) {
        if (cachedDocIds.contains(docId)) {
            cache.refresh(docId);
        }
    }

    @Override
    public void evict(Id docId) {
        cache.invalidate(docId);
    }

    @Override
    public void evict(Iterator<Id> docIds) {
        cache.invalidate(docIds);
    }

    @Override
    public void evictAll() {
        cache.invalidateAll();
    }

    protected CacheLoader<Id, ObjectVersionGraph<M>> newCacheLoader(final VersionStore<Id, M> versionStore) {
        return new CacheLoader<Id, ObjectVersionGraph<M>>() {

            @Override
            public ObjectVersionGraph<M> load(Id docId) throws Exception {
                log.debug("load({})", docId);
                return compactIfRequired(versionStore.getOptimizedGraph(docId));
            }

            @Override
            public ListenableFuture<ObjectVersionGraph<M>> reload(Id docId, ObjectVersionGraph<M> oldValue) throws Exception {
                if (!oldValue.isEmpty()) {
                    ObjectVersionGraph<M> newValue = oldValue;
                    Revision since = oldValue.getTip().getRevision();
                    try {
                        List<ObjectVersion<M>> updates = versionStore.fetchUpdates(docId, since);
                        log.debug("refresh({}): {})", docId, updates.size());
                        if (!updates.isEmpty()) {
                            newValue = oldValue.commit(updates);
                        }
                        return immediateFuture(compactIfRequired(newValue));
                    } catch (VersionNotFoundException e) {
                        // since revision is deleted - reload graph
                    }
                }
                return immediateFuture(load(docId));
            }

            private ObjectVersionGraph<M> compactIfRequired(ObjectVersionGraph<M> graph) {
                if (graphOptions.optimizeWhen.test(graph)) {
                    return graph.optimize(graphOptions.optimizeKeep.apply(graph)).getGraph();
                } else {
                    return graph;
                }
            }

        };
    }

}
