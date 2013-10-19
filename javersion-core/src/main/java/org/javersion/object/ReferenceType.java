package org.javersion.object;

import org.javersion.path.PropertyPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.util.Check;


public final class ReferenceType<V> implements ValueType<V> {

    private final FieldDescriptor idField;
    
    private final PropertyPath referencePath;
    
    private final PropertyPath idFieldPath;
    
    public ReferenceType(FieldDescriptor idField) {
        this.idField = Check.notNull(idField, "idField");
        this.referencePath = PropertyPath.ROOT.property("@").property(idField.getAnnotation(Id.class).alias());
        this.idFieldPath = referencePath.index("").property(idField.getName());
    }
    
    public String toString(ValueMapping<V> rootMapping, Object object) {
        return getIdType(rootMapping).toString(rootMapping, idField.get(object));
    }
    
    private ValueType<V> getIdType(ValueMapping<V> rootMapping) {
        return rootMapping.get(idFieldPath).valueType;
    }
    
    @Override
    public void serialize(Object object, SerializationContext<V> context) {
        PropertyPath path = context.getCurrentPath();
        if (object == null) {
            context.put(path, null);
        } else {
            Object idValue = idField.get(object);
            ValueType<V> idType = getIdType(context.getRootMapping());
            idType.serialize(idValue, context);
            if (!context.isSerialized(object)) {
                String id = idType.toString(context.getRootMapping(), idValue);
                context.serialize(referencePath.index(id), object);
            }
        }
    }

}
