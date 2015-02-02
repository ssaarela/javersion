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

import static org.javersion.core.Diff.diff;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.javersion.core.Merge;
import org.javersion.core.Version;
import org.javersion.json.JsonSerializer;
import org.javersion.object.ObjectSerializer;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionBuilder;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.object.TypeMappings;
import org.javersion.path.PropertyPath;
import org.javersion.store.ObjectVersionStoreJdbc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonStoreController {

    @Inject
    ObjectVersionStoreJdbc<Void> objectVersionStore;

    private final TypeMappings typeMappings = TypeMappings.builder().withMapping(new VersionPropertyMapping()).build();

    private final ObjectSerializer<VersionReference> metaSerializer = new ObjectSerializer<>(VersionReference.class, typeMappings);

    private JsonSerializer jsonSerializer = new JsonSerializer(metaSerializer.schemaRoot);

    @RequestMapping(value = "/objects/{objectId}", method = PUT)
    public ResponseEntity<String> put(@PathVariable("objectId") String objectId, @RequestBody String json) {
        JsonSerializer.JsonPaths paths = jsonSerializer.parse(json);
        VersionReference ref = metaSerializer.fromPropertyMap(paths.meta);
        ObjectVersionGraph<Void> versionGraph = objectVersionStore.load(objectId, null);
        ObjectVersionBuilder<Void> versionBuilder = new ObjectVersionBuilder<>();
        if (ref != null) {
            versionBuilder.parents(ref._revs);
        }
        versionBuilder.changeset(paths.properties, versionGraph);
        ObjectVersion<Void> version = versionBuilder.build();
        objectVersionStore.append(objectId, version);
        objectVersionStore.commit();
        return get(objectId);
    }

    @RequestMapping(value = "/objects/{objectId}", method = GET)
    public ResponseEntity<String> get(@PathVariable("objectId") String objectId) {
        ObjectVersionGraph<Void> versionGraph = objectVersionStore.load(objectId, null);
        if (versionGraph.isEmpty()) {
            return new ResponseEntity<String>(NOT_FOUND);
        }
        return getResponse(objectId, versionGraph);
    }

    private ResponseEntity<String> getResponse(String objectId, ObjectVersionGraph<Void> versionGraph) {
        Merge<PropertyPath, Object, Void> merge = versionGraph.mergeBranches(Version.DEFAULT_BRANCH);
        Map<PropertyPath, Object> properties = new HashMap<>(merge.getProperties());
        VersionReference ref = new VersionReference(objectId, merge.getMergeHeads(), merge.conflicts);
        properties.putAll(metaSerializer.toPropertyMap(ref));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json;charset=UTF-8");
        return new ResponseEntity<String>(jsonSerializer.serialize(properties), headers, OK);
    }

}
