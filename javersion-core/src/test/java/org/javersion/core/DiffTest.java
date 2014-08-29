package org.javersion.core;

import java.util.Map;

import static org.javersion.core.Diff.diff;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.google.common.collect.Maps;

public class DiffTest {
    
    @Test
    public void No_Changes() {
        Map<Object, Object> diff = diff(map(1,1, 2,2), map(1,1, 2,2));
        assertThat(diff.entrySet(), empty());
    }
    
    @Test
    public void Nulls_Ignored_Against_Empty() {
        Map<Object, Object> diff = diff(map(), map(1,null, 2,null));
        assertThat(diff.entrySet(), empty());
    }
    
    @Test
    public void Null_Is_Meaningfull_Against_Value() {
        Map<Object, Object> diff = diff(map(1,1), map(1,null));
        assertThat(diff, equalTo(map(1, null)));
    }
    
    @Test
    public void Null_Is_Meaningfull_Against_Value2() {
        Map<Object, Object> diff = diff(map(1,1, 2,2), map(1,null));
        assertThat(diff, equalTo(map(1, null, 2,null)));
    }
    
    @Test
    public void Missin_Value_Is_Null() {
        Map<Object, Object> diff = diff(map(1,1, 2,2), map());
        assertThat(diff, equalTo(map(1,null, 2,null)));
    }
    
    private static Map<Object, Object> map(Object... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected even keysAndValues.size()");
        }
        Map<Object, Object> map = Maps.newHashMap();
        for (int i=0; i < keysAndValues.length; i+=2) {
            map.put(keysAndValues[i], keysAndValues[i+1]);
        }
        return map;
    }
}
