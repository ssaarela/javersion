package org.javersion.util;

import org.junit.Test;

public class MutableHashMapTest {

    @Test
    public void Add_Remove_and_Add() {
        MutableHashMap<Integer, Integer> map = new MutableHashMap<>();
        map.put(1, 1);
        map.remove(1);
        map.put(1, 1);
    }
    
}
