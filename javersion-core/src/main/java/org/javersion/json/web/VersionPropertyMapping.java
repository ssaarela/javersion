package org.javersion.json.web;

import java.util.Optional;

import org.javersion.core.Revision;
import org.javersion.core.VersionProperty;
import org.javersion.object.DescribeContext;
import org.javersion.object.LocalTypeDescriptor;
import org.javersion.object.Persistent;
import org.javersion.object.ReadContext;
import org.javersion.object.WriteContext;
import org.javersion.object.mapping.TypeMapping;
import org.javersion.object.types.ValueType;
import org.javersion.path.PropertyPath;
import org.javersion.path.PropertyPath.NodeId;
import org.javersion.path.PropertyTree;
import org.javersion.reflect.TypeDescriptor;

public class VersionPropertyMapping implements TypeMapping {

    private static class VersionPropertyValueType implements ValueType {

        private static final String REVISION = "revision";
        private static final NodeId REVISION_ID = NodeId.valueOf(REVISION);

        private static final String VALUE = "value";
        private static final NodeId VALUE_ID = NodeId.valueOf(VALUE);

        @Override
        public Object instantiate(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
            Revision revision = (Revision) context.getObject(propertyTree.get(REVISION_ID));
            Object value = context.getProperty(propertyTree.get(VALUE_ID));
            return new VersionProperty<>(revision, value);
        }

        @Override
        public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            VersionProperty<?> versionProperty = (VersionProperty) object;
            context.put(path, Persistent.object());
            context.put(path.property(REVISION), versionProperty.revision.toString());
            context.put(path.property(VALUE), versionProperty.value);
        }

    }

    @Override
    public boolean applies(Optional<PropertyPath> path, LocalTypeDescriptor localTypeDescriptor) {
        return path.isPresent() && localTypeDescriptor.typeDescriptor.getRawType().equals(VersionProperty.class);
    }

    @Override
    public ValueType describe(Optional<PropertyPath> path, TypeDescriptor type, DescribeContext context) {
        context.describeAsync(path.get().property("revision"), type.getField("revision"));
        return new VersionPropertyValueType();
    }
}
