package org.javersion.object;


public class ValueTypes<V> {

    private final Iterable<ValueType<V>> types;
    
    public ValueTypes(Iterable<ValueType<V>> types) {
        this.types = types;
    }

    public ValueType<V> get(ValueMappingKey mappingKey) {
        for (ValueType<V> valueType : types) {
            if (valueType.applies(mappingKey)) {
                return valueType;
            }
        }
        throw new IllegalArgumentException("ValueType not found for " + mappingKey);
    }
    
}
