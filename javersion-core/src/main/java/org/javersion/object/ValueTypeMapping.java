package org.javersion.object;


public interface ValueTypeMapping<V> {

    boolean applies(ValueMappingKey mappingKey);
    
    ValueType<V> describe(DescribeContext<V> context);

}
