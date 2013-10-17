package org.javersion.object;

import java.util.Map;

public interface ValueType<V> {

    boolean applies(ValueMappingKey mappingKey);
    
    Map<String, ValueMapping<V>> describe(DescribeContext<V> context);
    
    void serialize(SerializationContext<V> context);

}
