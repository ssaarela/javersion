package org.javersion.object;


public interface ValueTypeFactory<V> {

    boolean applies(ValueMappingKey mappingKey);
    
    ValueType<V> describe(DescribeContext<V> context);

}
