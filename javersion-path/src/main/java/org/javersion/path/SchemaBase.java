package org.javersion.path;

import static com.google.common.base.Preconditions.checkNotNull;

import org.javersion.path.PropertyPath.NodeId;
import org.javersion.util.Check;

import com.google.common.base.Preconditions;

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
