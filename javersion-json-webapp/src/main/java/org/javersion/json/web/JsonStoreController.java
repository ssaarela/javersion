/*
 * Copyright 2014 Samppa Saarela
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
package org.javersion.json.web;

import static org.javersion.core.Version.DEFAULT_BRANCH;
import static org.javersion.path.PropertyPath.ROOT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.javersion.core.Merge;
import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.json.JsonSerializer;
import org.javersion.object.*;
import org.javersion.path.PropertyPath;
import org.javersion.store.jdbc.ObjectVersionStoreJdbc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableSet;

/**
 * GET: /objects/{objectId} - get default branch
 * PUT: /objects/{objectId} - update default branch
 *
 * GET: /objects/{objectId}/{branch|revision}
 * PUT: /objects/{objectId}/{branch|revision}
 *
 * GET: /versions/{objectId}
 * PUT: /versions/{objectId} - rewrite
 */
@RestController
public class JsonStoreController {

    private final JsonStoreConfig config;

    @Inject
    ObjectVersionStoreJdbc<Void> objectVersionStore;

    private final TypeMappings metaTypeMappings = TypeMappings.builder()
            .withMapping(new VersionPropertyMapping())
            .build();

    private final ObjectSerializer<VersionMetadata> metaSerializer = new ObjectSerializer<>(VersionMetadata.class, metaTypeMappings);

    private final JsonSerializer jsonSerializer = new JsonSerializer(new JsonSerializer.Config(false, false, ""), metaSerializer.schemaRoot);

    public JsonStoreController() {
        this(new JsonStoreConfig());
    }

    public JsonStoreController(JsonStoreConfig config) {
        this.config = config;
    }

    @RequestMapping(value = "/objects", method = POST)
    public ResponseEntity<String> postObject(@RequestBody String json) {
        String objectId = UUID.randomUUID().toString();
        return putObject(objectId, json, DEFAULT_BRANCH, true);
    }

    @RequestMapping(value = "/objects/{objectId}", method = PUT)
    public ResponseEntity<String> putObject(@PathVariable("objectId") String objectId, @RequestBody String json) {
        return putObject(objectId, json, DEFAULT_BRANCH, false);
    }

    @RequestMapping(value = "/objects/{objectId}/branches/{branch}", method = PUT)
    public ResponseEntity<String> putObjectOnBranch(@PathVariable("objectId") String objectId,
                                            @PathVariable("branch") String branch,
                                            @RequestBody String json) {
        return putObject(objectId, json, branch, false);
    }

    @RequestMapping(value = "/objects/{objectId}", method = GET)
    public ResponseEntity<String> getObject(@PathVariable("objectId") String objectId,
                                            @RequestParam(value = "merge", required = false) Set<String> merge) {
        return getBranch(objectId, DEFAULT_BRANCH, merge);
    }

    @RequestMapping(value = "/objects/{objectId}/branches/{branch}", method = GET)
    public ResponseEntity<String> getBranch(@PathVariable("objectId") String objectId,
                                            @PathVariable("branch") String branch,
                                            @RequestParam(value = "merge", required = false) Set<String> merge) {
        if (merge == null) {
            return getObject(objectId, null, branchesBuilder().add(branch).build(), false);
        }
        return getObject(objectId, null, branchesBuilder().add(branch).addAll(merge).build(), false);
    }

    @RequestMapping(value = "/objects/{objectId}/versions/{revision}", method = GET)
    public ResponseEntity<String> getVersion(@PathVariable("objectId") String objectId,
                                             @PathVariable("revision") Revision revision,
                                             @RequestParam(value = "merge", required = false) Set<String> merge) {
        if (merge == null) {
            return getObject(objectId, revision, branchesBuilder().build(), false);
        }
        return getObject(objectId, revision, branchesBuilder().addAll(merge).build(), false);
    }

    @RequestMapping(value = "/objects/{objectId}/versions", method = GET)
    public List<Version<PropertyPath, Object, Void>> getVersions(@PathVariable("objectId") String objectId) {
        ObjectVersionGraph<Void> versionGraph = objectVersionStore.load(objectId);
        if (versionGraph.isEmpty()) {
            throw new NotFoundException();
        }

        return versionGraph.getVersions();
    }

    private ResponseEntity<String> getObject(String objectId,
                                            Revision revision,
                                            Set<String> mergeBranches,
                                            boolean create) {
        ObjectVersionGraph<Void> versionGraph = objectVersionStore.load(objectId);
        if (versionGraph.isEmpty()) {
            throw new NotFoundException();
        }
        return getResponse(objectId, versionGraph, revision, mergeBranches, create);
    }

    private ResponseEntity<String> putObject(String objectId,
                                             String json,
                                             String branch,
                                             boolean create) {
        JsonSerializer.JsonPaths paths = jsonSerializer.parse(json);
        if (!(paths.properties.get(ROOT) instanceof Persistent.Object)) {
            throw new IllegalArgumentException("Expected root object to be an Object, got " + paths.properties.get(ROOT));
        }
        VersionMetadata ref = metaSerializer.fromPropertyMap(paths.meta);
        ObjectVersionGraph<Void> versionGraph = objectVersionStore.load(objectId);
        ObjectVersionBuilder<Void> versionBuilder = new ObjectVersionBuilder<>();
        if (ref != null && ref._revs != null) {
            versionBuilder.parents(ref._revs);
        } else if (!versionGraph.isEmpty()) {
            versionBuilder.parents(getParentsForUnreferencedUpdate(versionGraph, branch));
        }
        versionBuilder.branch(branch);
        versionBuilder.changeset(paths.properties, versionGraph);
        ObjectVersion<Void> version = versionBuilder.build();
        objectVersionStore.append(objectId, version);
        objectVersionStore.commit();
        return getObject(objectId, null, ImmutableSet.of(branch), create);
    }

    private Iterable<Revision> getParentsForUnreferencedUpdate(ObjectVersionGraph<Void> versionGraph, String branch) {
        for (JsonStoreConfig.UnreferencedUpdateStrategy strategy : config.unreferencedUpdateStrategy) {
            Iterable<Revision> parents = strategy.getParents(versionGraph, branch);
            if (parents != null) {
                return parents;
            }
        }
        throw new IllegalArgumentException("_revs missing, unreferenced update not allowed");
    }

    private ResponseEntity<String> getResponse(String objectId,
                                               ObjectVersionGraph<Void> versionGraph,
                                               Revision revision,
                                               Set<String> mergeBranches,
                                               boolean create) {
        if (revision != null) {
            versionGraph = versionGraph.at(revision);
        }
        if (mergeBranches.isEmpty()) {
            // Specific version requested
            return getResponse(objectId, versionGraph.getVersionNode(revision), create);
        } else {
            // If revision is given, merge branches of that time
            return getResponse(objectId, versionGraph.mergeBranches(mergeBranches), create);
        }
    }

    private ResponseEntity<String> getResponse(String objectId, Merge<PropertyPath, Object, Void> merge, boolean create) {
        Map<PropertyPath, Object> properties = new HashMap<>(merge.getProperties());
        VersionMetadata ref = new VersionMetadata(objectId, merge.getMergeHeads(), merge.conflicts);
        properties.putAll(metaSerializer.toPropertyMap(ref));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json;charset=UTF-8");
        HttpStatus status;
        if (create) {
            status = CREATED;
            try {
                headers.setLocation(new URI("/objects/" + objectId));
            } catch (URISyntaxException e) {
                throw new Error(e);
            }
        } else {
            status = OK;
        }
        return new ResponseEntity<String>(jsonSerializer.serialize(properties), headers, status);
    }

    private static ImmutableSet.Builder<String> branchesBuilder() {
        return ImmutableSet.<String>builder();
    }

}
