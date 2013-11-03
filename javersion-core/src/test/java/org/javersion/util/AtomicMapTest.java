package org.javersion.util;

import static org.javersion.util.PersistentSetTest.SET;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class AtomicMapTest {

    private static final PersistentMap<Integer, Integer> MAP;

    static {
        PersistentMap<Integer, Integer> map = PersistentMap.empty();
        for (Integer integer : SET) {
            map = map.assoc(integer, integer);
        }
        MAP = map;
    }

    AtomicMap<Integer, Integer> map = MAP.toAtomicMap();

    @Test
    public void Expected_Content() {
        assertThat(map.size(), equalTo(SET.size()));
        for (Integer integer : SET) {
            assertThat(map.get(integer), equalTo(integer));
        }
    }
    
    @Test
    public void Entry_Set() {
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            assertThat(SET.contains(entry.getKey()), equalTo(true));
        }
    }

    @Test
    public void Remove() {
        for (Integer integer : SET) {
            assertThat(map.remove(integer), equalTo(integer));
        }
    }
    
    @Test
    public void Clear() {
        map.clear();
        assertThat(map.size(), equalTo(0));
    }
    
    @Test
    public void Put_All() {
        Map<Integer, Integer> additions = new HashMap<>();
        for (int i=0; additions.size() < 10; i++) {
            Integer integer = i;
            if (!SET.contains(integer)) {
                additions.put(integer, integer);
            }
        }
        map.putAll(additions);
        assertThat(map.size(), equalTo(SET.size() + 10));
        for (Integer integer : additions.keySet()) {
            assertThat(map.containsKey(integer), equalTo(true));
        }
    }
    
    @Test
    public void Put() {
        Integer integer = SET.iterator().next();
        assertThat(map.put(integer, 1), equalTo(integer));
        assertThat(map.get(integer), equalTo(Integer.valueOf(1)));
    }
}
