package org.javersion.object.types;

public interface ScalarType extends IdentifiableType {

    public Object fromString(String str) throws Exception;

}
