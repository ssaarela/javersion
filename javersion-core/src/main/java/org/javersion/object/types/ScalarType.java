package org.javersion.object.types;

import org.javersion.path.PropertyPath.NodeId;

public interface ScalarType extends IdentifiableType {

    Object fromNodeId(NodeId nodeId);

}
