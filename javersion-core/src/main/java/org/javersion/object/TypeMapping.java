package org.javersion.object;


public interface TypeMapping {

    boolean applies(TypeMappingKey mappingKey);
    
    ValueType describe(DescribeContext context);

}
