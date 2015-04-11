package org.javersion.object.types;

import org.javersion.object.ReadContext;
import org.javersion.path.PropertyPath.NodeId;

public interface ScalarType extends IdentifiableType {

    Object fromNodeId(NodeId nodeId, ReadContext context) throws Exception;

}
