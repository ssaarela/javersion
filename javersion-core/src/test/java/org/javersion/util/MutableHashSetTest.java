package org.javersion.util;

import org.junit.Test;

public class MutableHashSetTest {

    @Test
    public void Add_Remove_and_Add() {
        MutableHashSet<Integer> map = new MutableHashSet<>();
        map.add(1);
        map.remove(1);
        map.add(1);
    }
    
}
