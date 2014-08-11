/*
 * Copyright 2013 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javersion.util;

import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.javersion.util.AbstractRedBlackTree.Color;
import org.javersion.util.AbstractTreeMap.Node;
import org.junit.Test;

public class PersistentTreeMapTest extends AbstractPersistentMapTest<PersistentTreeMap<Integer, Integer>> {

    @Test
    public void Iterate_Random() {
        PersistentTreeMap<Integer, Integer> map = emptyMap();
        for (Integer kv : randoms(1234)) {
            map = map.assoc(kv, kv);
        }
        Iterator<Map.Entry<Integer, Integer>> iter = map.iterator();
        int count = 0;
        Integer prev = null;
        Integer next;
        while (iter.hasNext()) {
            count++;
            if (prev == null) {
                prev = iter.next().getKey();
            } else {
                next = iter.next().getKey();
                assertThat(prev, lessThan(next));
                prev = next;
            }
        }
        assertThat(count, equalTo(1234));
    }
    
    @Test
    public void Iterate_Ascending() {
        PersistentTreeMap<Integer, Integer> pmap = emptyMap();
        for (int kv = 1; kv < 10; kv++) {
            pmap = pmap.assoc(kv, kv);
        }
        assertThat(keys(pmap), contains(1,2,3,4,5,6,7,8,9));
    }
    
    @Test
    public void Iterate_Descending() {
        PersistentTreeMap<Integer, Integer> pmap = emptyMap();
        for (int kv = 1; kv < 10; kv++) {
            pmap = pmap.assoc(kv, kv);
        }
        final PersistentTreeMap<Integer, Integer> map = pmap;
        assertThat(keys(new Iterable<Map.Entry<Integer, Integer>>() {
            @Override
            public Iterator<Entry<Integer, Integer>> iterator() {
                return map.iterator(false);
            }
        }), contains(1,2,3,4,5,6,7,8,9));
    }
    
    @Test
    public void Iterate_Ascending_Range() {
        PersistentTreeMap<Integer, Integer> map = mapForRangeTest();
        assertThat(keys(map.range(3, 9)), contains(3, 5, 7));
        assertThat(keys(map.range(3, true, 9, true)), contains(3, 5, 7, 9));
        assertThat(keys(map.range(3, false, 9, false)), contains(5, 7));
        assertThat(keys(map.range(9, 10)), contains(9));
        assertThat(keys(map.range(-5, 2)), contains(1));
        assertThat(keys(map.range(6, 7)), emptyIterable());
        assertThat(keys(map.range(-10, 1)), emptyIterable());
        assertThat(keys(map.range(9, false, 20, false)), emptyIterable());
    }

    private PersistentTreeMap<Integer, Integer> mapForRangeTest() {
        PersistentTreeMap<Integer, Integer> map = emptyMap();
        for (int kv = 1; kv < 10; kv += 2) {
            map = map.assoc(kv, kv);
        }
        return map;
    }
    
    @Test
    public void Iterate_Descending_Range() {
        PersistentTreeMap<Integer, Integer> map = mapForRangeTest();
        assertThat(keys(map.range(3, true, 9, false, false)), contains(7, 5, 3));
        assertThat(keys(map.range(3, true, 9, true, false)), contains(9, 7, 5, 3));
        assertThat(keys(map.range(3, false, 9, false, false)), contains(7, 5));
    }
    
    @Test
    public void Iterate_Ascending_Tail_Map() {
        PersistentTreeMap<Integer, Integer> map = mapForRangeTest();
        assertThat(keys(map.range(7, true, null, false, true)), contains(7, 9));
    }
    
    @Test
    public void Iterate_Descending_Tail_Map() {
        PersistentTreeMap<Integer, Integer> map = mapForRangeTest();
        assertThat(keys(map.range(7, true, null, false, false)), contains(9, 7));
    }
    
    @Test
    public void Iterate_Ascending_Head_Map() {
        PersistentTreeMap<Integer, Integer> map = mapForRangeTest();
        assertThat(keys(map.range(null, true, 5, true, true)), contains(1, 3, 5));
        assertThat(keys(map.range(null, true, 5, false, true)), contains(1, 3));
    }
    
    @Test
    public void Iterate_Descending_Head_Map() {
        PersistentTreeMap<Integer, Integer> map = mapForRangeTest();
        assertThat(keys(map.range(null, true, 5, true, false)), contains(5, 3, 1));
        assertThat(keys(map.range(null, true, 5, false, false)), contains(3, 1));
    }
    
    private static Iterable<Integer> keys(Iterable<Map.Entry<Integer, Integer>> entries) {
        return transform(entries, MapUtils.<Integer>mapKeyFunction());
    }
    
    @Test
    public void CLR_P269() {
        // Example tree
        assertInsert(
                11,
                2,
                14,
                1,
                7,
                15,
                5,
                8,
                4
                );
        // Same nodes in ascending order
        assertInsert(
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
    }
    
    @Test
    public void Find_Min_Max() {
        List<Integer> ints = ascending(37);
        PersistentTreeMap<Integer, Integer> pmap = emptyMap();
        for (Integer kv : ints) {
            pmap = pmap.assoc(kv, kv);
        }
        assertThat(pmap.max(), equalTo(ints.get(36)));
        assertThat(pmap.min(), equalTo(ints.get(0)));
    }
    
    @Test
    public void Find_Min_Max_From_Empty() {
        PersistentTreeMap<Integer, Integer> pmap = emptyMap();
        assertThat(pmap.max(), nullValue());
        assertThat(pmap.min(), nullValue());
    }

    private static Integer blacksOnPath = null;
    
    static synchronized void assertNodeProperties(Node<Integer, Integer> node) {
        blacksOnPath = null;
        if (node != null) {
            assertNodeProperties(node, 0);
        }
    }
    
    private static void assertNodeProperties(Node<Integer, Integer> node, int blacks) {
        assertThat(node.color, not(nullValue()));
        if (node.color == Color.RED) {
            assertBlack(node.left);
            assertBlack(node.right);
        } else {
            blacks++;
        }
        boolean leaf = true;
        if (node.left != null){
            assertThat(node.left.key, lessThan(node.key));
            assertNodeProperties(node.left, blacks);
            leaf = false;
        }
        if (node.right != null) {
            assertThat(node.right.key, greaterThan(node.key));
            assertNodeProperties(node.right, blacks);
            leaf = false;
        }
        if (leaf) {
            if (blacksOnPath == null) {
                blacksOnPath = blacks;
            } else {
                assertThat(blacks, equalTo(blacksOnPath.intValue()));
            }
        }
    }

    static void assertBlack(Node<?, ?> node) {
        assertTrue("Expected black node (or null)", node == null || node.color == Color.BLACK);
    }

    @Override
    protected PersistentTreeMap<Integer, Integer> emptyMap() {
        return PersistentTreeMap.<Integer, Integer>empty();
    }

    @Override
    protected void assertMapProperties(PersistentMap<Integer, Integer> map) {
        assertNodeProperties(((PersistentTreeMap<Integer, Integer>) map).root());
    }

    @Override
    protected void assertEmptyMap(PersistentMap<Integer, Integer> map) {
        assertThat(map.size(), equalTo(0));
        assertThat(((PersistentTreeMap<Integer, Integer>) map).root(), nullValue());
    }
}
