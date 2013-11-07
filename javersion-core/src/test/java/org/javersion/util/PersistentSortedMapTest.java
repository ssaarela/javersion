package org.javersion.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class PersistentSortedMapTest {
    
    @Test
    public void One_Entry_Immutability() {
        PersistentSortedMap<Integer, Integer> map = PersistentSortedMap.empty();
        PersistentSortedMap<Integer, Integer> newMap = map.assoc(5, 5);
        
        assertThat(newMap, not(sameInstance(map)));
        
        assertThat(map.size(), equalTo(0));
        assertThat(newMap.size(), equalTo(1));
        
        assertThat(map.get(5), nullValue());
        assertThat(newMap.get(5), equalTo(5));
        
        map = newMap;
        newMap = map.assoc(5, 6);
        
        assertThat(newMap, not(sameInstance(map)));
        
        assertThat(map.size(), equalTo(1));
        assertThat(newMap.size(), equalTo(1));
        
        assertThat(map.get(5), equalTo(5));
        assertThat(newMap.get(5), equalTo(6));
    }
    
    @Test
    public void Insert_On_Both_Sides() {
        PersistentSortedMap<Integer, Integer> map = PersistentSortedMap.empty();
        map = assoc(map, 5);
        map = assoc(map, 7);
        map = assoc(map, 3);
        map = assoc(map, 1);
        map = assoc(map, 4);
        map = assoc(map, 2);
        map = assoc(map, 8);
        map = assoc(map, 6);

        for (int i=1; i <= 8; i++) {
            assertThat(map.get(i), equalTo(i));
        }
    }
    
    @Test
    public void Decreasing_Inserts() {
        PersistentSortedMap<Integer, Integer> map = PersistentSortedMap.empty();
        for (int i=16; i > 0; i--) {
            map = assoc(map, i);
        }
        for (int i=16; i > 0; i--) {
            assertThat(map.get(i), equalTo(i));
        }
        System.out.println(map);
    }
    
    @Test
    public void CLR_P269() {
        PersistentSortedMap<Integer, Integer> map = PersistentSortedMap.empty();
        List<Integer> ints = ImmutableList.of(
                1,
                2,
                4,
                5,
                7,
                8,
                11,
                14,
                15
                );
        for (Integer i : ints) {
            map = assoc(map, i);
        }
        
        assertThat(map.size(), equalTo(ints.size()));

        for (Integer i : ints) {
            assertEntry(map, i);
        }
    }
    
    private void assertEntry(PersistentSortedMap<Integer, Integer> map, Integer i) {
        assertThat(map.get(i), equalTo(i));
    }
    
    private PersistentSortedMap<Integer, Integer> assoc(PersistentSortedMap<Integer, Integer> map, Integer i) {
        return map.assoc(i, i);
    }
}
