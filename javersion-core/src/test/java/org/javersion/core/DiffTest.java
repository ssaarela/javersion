package org.javersion.core;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.javersion.core.Diff.diff;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import com.google.common.collect.Maps;

public class DiffTest {

    @Test
    public void No_Changes() {
        Map<Object, Object> diff = diff(map(1, 1, 2, 2), map(1, 1, 2, 2));
        assertThat(diff.entrySet(), empty());
    }

    @Test
    public void Nulls_Ignored_Against_Empty() {
        Map<Object, Object> diff = diff(map(), map(1, null, 2, null));
        assertThat(diff.entrySet(), empty());
    }

    @Test
    public void Null_Is_Meaningful_Against_Value() {
        Map<Object, Object> diff = diff(map(1, 1), map(1, null));
        assertThat(diff, equalTo(map(1, null)));
    }

    @Test
    public void Null_Is_Meaningful_Against_Value2() {
        Map<Object, Object> diff = diff(map(1, 1, 2, 2), map(1, null));
        assertThat(diff, equalTo(map(1, null, 2, null)));
    }

    @Test
    public void Missing_Value_Is_Null() {
        Map<Object, Object> diff = diff(map(1, 1, 2, 2), map());
        assertThat(diff, equalTo(map(1,null, 2,null)));
    }

    @Test
    public void Values_Against_Empty() {
        Map<Object, Object> diff = diff(map(), map(1, 1, 2, 2));
        assertThat(diff, equalTo(map(1,1, 2,2)));
    }

    @Test
    public void All_Different_1() {
        Map<Object, Object> diff = diff(map(1, 1, 2, 2, 3, 3), map(4, 4, 5, 5));
        assertThat(diff, equalTo(map(1,null, 2,null, 3,null, 4,4, 5,5)));
    }

    @Test
    public void All_Different_2() {
        Map<Object, Object> diff = diff(map(1,1, 2,2), map(3,3, 4,4, 5,5));
        assertThat(diff, equalTo(map(1,null, 2,null, 3,3, 4,4, 5,5)));
    }

    @Test
    public void Sorted_Equal() {
        Map<Object, Object> diff = diff(sorted(1, 1, 2, 2), sorted(1, 1, 2, 2));
        assertThat(diff, equalTo(sorted()));
    }

    @Test
    public void Sorted_All_Higher() {
        Map<Object, Object> diff = diff(sorted(1,1, 2,2), sorted(3,3, 4,4));
        assertThat(diff, equalTo(sorted(1,null, 2,null, 3,3, 4,4)));
    }

    @Test
    public void Sorted_All_Lower() {
        Map<Object, Object> diff = diff(sorted(3,3, 4,4), sorted(1,1, 2,2));
        assertThat(diff, equalTo(sorted(1,1, 2,2, 3,null, 4,null)));
    }

    @Test
    public void Sorted_Overlapping() {
        Map<Object, Object> diff = diff(sorted(1,1, 2,2), sorted(2,2, 3,3));
        assertThat(diff, equalTo(sorted(1,null, 3,3)));
    }

    @Test
    public void Sorted_Empty_From() {
        Map<Object, Object> diff = diff(sorted(), sorted(2,2, 3,3));
        assertThat(diff, equalTo(sorted(2,2, 3,3)));
    }

    @Test
    public void Sorted_Empty_To() {
        Map<Object, Object> diff = diff(sorted(2,2, 3,3), sorted());
        assertThat(diff, equalTo(sorted(2,null, 3,null)));
    }

    public static <K> Map<K, K> map(K... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected even keysAndValues.size()");
        }
        Map<K, K> map = Maps.newHashMap();
        for (int i=0; i < keysAndValues.length; i+=2) {
            map.put(keysAndValues[i], keysAndValues[i+1]);
        }
        return map;
    }

    public static <K> SortedMap<K, K> sorted(K... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected even keysAndValues.size()");
        }
        SortedMap<K, K> map = new TreeMap<>();
        for (int i=0; i < keysAndValues.length; i+=2) {
            map.put(keysAndValues[i], keysAndValues[i+1]);
        }
        return map;
    }

}
