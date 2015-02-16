package org.javersion.json.web;

import static org.javersion.object.Persistent.Type.ARRAY;
import static org.javersion.object.Persistent.Type.OBJECT;

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
import org.javersion.path.PropertyTree;
import org.javersion.reflect.TypeDescriptor;

public class VersionPropertyMapping implements TypeMapping {

    private static class VersionPropertyValueType implements ValueType {

        @Override
        public Object instantiate(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {
            Revision revision = (Revision) context.getObject(propertyTree.get("revision"));
            Object value = context.getProperty(propertyTree.get("value"));
            return new VersionProperty<>(revision, value);
        }

        @Override
        public void bind(PropertyTree propertyTree, Object object, ReadContext context) throws Exception {}

        @Override
        public void serialize(PropertyPath path, Object object, WriteContext context) {
            VersionProperty<?> versionProperty = (VersionProperty) object;
            context.put(path, Persistent.object());
            context.put(path.property("revision"), versionProperty.revision.toString());
            context.put(path.property("value"), versionProperty.value);
        }

        @Override
        public boolean isReference() {
            return false;
        }

    }

    @Override
    public boolean applies(PropertyPath path, LocalTypeDescriptor localTypeDescriptor) {
        return localTypeDescriptor.typeDescriptor.getRawType().equals(VersionProperty.class);
    }

    @Override
    public ValueType describe(PropertyPath path, TypeDescriptor type, DescribeContext context) {
        context.describeAsync(path.property("revision"), type.getField("revision"));
        return new VersionPropertyValueType();
    }
}
