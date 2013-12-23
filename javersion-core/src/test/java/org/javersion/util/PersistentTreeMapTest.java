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

import org.javersion.util.AbstractRedBlackTree.Color;
import org.javersion.util.AbstractTreeMap.Node;
import org.junit.Test;

public class PersistentTreeMapTest extends AbstractPersistentMapTest<PersistentTreeMap<Integer, Integer>> {

    @Test
    public void Iterate_Random() {
        PersistentMap<Integer, Integer> map = emptyMap();
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
