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
package org.javersion.path;

import org.javersion.path.PropertyPath.NodeId;
import org.javersion.util.Check;

public abstract class SchemaBase<This extends SchemaBase<This>> {

    public abstract This getChild(NodeId nodeId);

    public This get(PropertyPath path) {
        This schema = find(path);
        if (schema == null) {
            throw new IllegalArgumentException("Path not found: " + path);
        }
        return schema;
    }

    public This find(PropertyPath path) {
        Check.notNull(path, "path");

        @SuppressWarnings("unchecked")
        This currentMapping = (This) this;
        for (PropertyPath currentPath : path.asList()) {
            NodeId nodeId = currentPath.getNodeId();
            @SuppressWarnings("unchecked")
            This childMapping = currentMapping.getChild(nodeId);
            while (childMapping == null && (nodeId = nodeId.fallbackId()) != null) {
                childMapping = currentMapping.getChild(nodeId);
            }
            if (childMapping != null) {
                currentMapping = childMapping;
            } else {
                return null;
            }
        }
        return currentMapping;
    }

}
