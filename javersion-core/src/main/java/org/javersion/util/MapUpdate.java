package org.javersion.util;


public interface MapUpdate<K, V> {

    public void apply(MutableMap<K, V> map);

}
