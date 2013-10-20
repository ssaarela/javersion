package org.javersion.object;

import org.javersion.path.PropertyPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.util.Check;


public final class ReferenceType<V> implements IndexableType<V> {

    private final FieldDescriptor idField;
    
    private final PropertyPath referencePath;
    
    private final PropertyPath idFieldPath;
    
    public ReferenceType(FieldDescriptor idField) {
        this.idField = Check.notNull(idField, "idField");
        this.referencePath = PropertyPath.ROOT.property("@").property(idField.getAnnotation(Id.class).alias());
        this.idFieldPath = referencePath.index("").property(idField.getName());
    }
    
    public String toString(Object object, ValueMapping<V> rootMapping) {
        return getIdType(rootMapping).toString(idField.get(object), rootMapping);
    }
    
    private IndexableType<V> getIdType(ValueMapping<V> rootMapping) {
        return (IndexableType<V>) rootMapping.get(idFieldPath).valueType;
    }
    
    @Override
    public void serialize(Object object, SerializationContext<V> context) {
        PropertyPath path = context.getCurrentPath();
        if (object == null) {
            context.put(path, null);
        } else {
            Object idValue = idField.get(object);
            IndexableType<V> idType = getIdType(context.getRootMapping());
            idType.serialize(idValue, context);
            if (!context.isSerialized(object)) {
                String id = idType.toString(idValue, context.getRootMapping());
                context.serialize(referencePath.index(id), object);
            }
        }
    }

}
