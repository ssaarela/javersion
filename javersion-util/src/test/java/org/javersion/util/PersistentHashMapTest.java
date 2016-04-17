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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javersion.util.AbstractHashTrie.ArrayNode;
import org.javersion.util.AbstractHashTrie.HashNode;
import org.javersion.util.AbstractHashTrie.Node;
import org.junit.Test;

import com.google.common.collect.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class PersistentHashMapTest extends AbstractPersistentMapTest<PersistentHashMap<Integer,Integer>>{

    private static <K, V> Merger<Map.Entry<K, V>> vetoMerger() {
        return new Merger<Map.Entry<K, V>>() {
            @Override
            public boolean merge(Map.Entry<K, V> oldEntry, Map.Entry<K, V> newEntry) {
                return false;
            }
            @Override
            public boolean insert(Map.Entry<K, V> newEntry) {
                return false;
            }
            @Override
            public boolean delete(Map.Entry<K, V> oldEntry) {
                return false;
            }
        };
    }

    static class HashKey {
        public final int hash;
        public HashKey(int hash) {
            this.hash = hash;
        }
        @Override
        public int hashCode() {
            return hash;
        }
        public String toString() {
            return "" + hash + "@" + System.identityHashCode(this);
        }
    }

//    @Test
//    public void Add_Values() {
//        PersistentHashMap<String, String> map = PersistentHashMap.empty();
//        PersistentHashMap<String, String> otherMap = map.assoc("key", "value");
//        assertThat(otherMap.get("key"), equalTo("value"));
//        assertThat(map.get("key"), nullValue());
//
//        map = otherMap.assoc("key", "value2");
//        assertThat(map.get("key"), equalTo("value2"));
//        assertThat(otherMap.get("key"), equalTo("value"));
//
//        map = map.assoc("key2", "value");
//        assertThat(map.get("key2"), equalTo("value"));
//        assertThat(otherMap.get("key2"), nullValue());
//
//        map = map.assoc("null", null);
//        assertThat(map.get("null"), nullValue());
//        assertThat(map.containsKey("null"), equalTo(true));
//
//        assertThat(map.containsKey(null), equalTo(false));
//        map = map.assoc(null, "not-null");
//        assertThat(map.get(null), equalTo("not-null"));
//    }

    @Test
    public void Size_With_Collisions() {
        HashKey k1 = new HashKey(1);
        HashKey k2 = new HashKey(1);
        HashKey k3 = new HashKey(1);

        PersistentHashMap<Object, Object> map = PersistentHashMap.empty();
        assertThat(map.size(), equalTo(0));

        map = map.assoc(k1, k1);
        assertThat(map.size(), equalTo(1));

        // Same key and value
        map = map.assoc(k1, k1);
        assertThat(map.size(), equalTo(1));

        // Same key, different value
        map = map.assoc(k1, k2);
        assertThat(map.size(), equalTo(1));

        // Colliding key
        map = map.assoc(k2, k2);
        assertThat(map.size(), equalTo(2));

        // Same colliding key and value
        map = map.assoc(k2, k2);
        assertThat(map.size(), equalTo(2));

        // Same colliding key, different value
        map = map.assoc(k2, k1);
        assertThat(map.size(), equalTo(2));

        // Another colliding key
        map = map.assoc(k3, k3);
        assertThat(map.size(), equalTo(3));

    }

    @Test
    public void Size_With_Deep_Collision() {
        HashKey k0 = new HashKey(0);
        HashKey k1 = new HashKey(0);

        PersistentHashMap<Object, Object> map = PersistentHashMap.empty();
        map = map.assoc(k0, k0);
        map = map.assoc(k1, k1);
        assertThat(map.size(), equalTo(2));

        for (int i=1; i < 32; i++) {
            map = map.assoc(i, i);
            assertThat(map.size(), equalTo(i + 2));
        }
        assertThat(map.size(), equalTo(33));
        map = map.assoc(32, 32);
        assertThat(map.size(), equalTo(34));
    }

    @Test
    public void Collision_Dissoc() {
        HashKey k0 = new HashKey(0);
        HashKey k1 = new HashKey(0);
        HashKey k2 = new HashKey(0);

        PersistentHashMap<Object, Object> map = PersistentHashMap.empty();
        map = map.assoc(k0, k0);
        map = map.assoc(k1, k1);

        assertThat(map.dissoc(0).size(), equalTo(2));

        assertThat(map.dissoc(k1).size(), equalTo(1));
        assertThat(map.dissoc(k1).get(k0), equalTo((Object) k0));

        assertThat(map.dissoc(k0).size(), equalTo(1));
        assertThat(map.dissoc(k0).get(k0), nullValue());

        map = map.assoc(k2, k2);
        assertThat(map.dissoc(k0).size(), equalTo(2));
        assertThat(map.dissoc(k0).get(k2), equalTo((Object) k2));
        assertThat(map.dissoc(k0).get(k1), equalTo((Object) k1));

        assertThat(map.dissoc(k1).size(), equalTo(2));
        assertThat(map.dissoc(k1).get(k0), equalTo((Object) k0));
        assertThat(map.dissoc(k1).get(k2), equalTo((Object) k2));

        assertThat(map.dissoc(k2).size(), equalTo(2));
        assertThat(map.dissoc(k2).get(k0), equalTo((Object) k0));
        assertThat(map.dissoc(k2).get(k1), equalTo((Object) k1));

        assertThat(map.dissoc(0), sameInstance(map));
    }

    @Test
    public void ArrayNode_insert() {
        PersistentMap<Integer, Integer> map = emptyMap(), result;
        for (int i=0; i < 32; i++) {
            map = map.assoc(i, i);
        }
        map = map.dissoc(7);
        map = map.assoc(7, 7);
        assertThat(map.containsKey(7), equalTo(true));

        map = map.dissoc(13);
        result = map.merge(13, 13, vetoMerger());
        assertThat(result, sameInstance(map));
    }

    @Test
    public void collisions() {
        HashKey k1 = new HashKey(1);
        HashKey k2 = new HashKey(1);
        HashKey k3 = new HashKey(1);

        PersistentHashMap<HashKey, HashKey> map = PersistentHashMap.empty();
        map = map.assoc(k1, k1);
        map = map.assoc(k2, k1);
        map = map.assoc(k2, k2);
        map = map.assoc(k3, k3);

        assertThat(map.get(k1), equalTo(k1));
        assertThat(map.get(k2), equalTo(k2));
        assertThat(map.get(k3), equalTo(k3));

        assertThat(map.get(new HashKey(1)), nullValue());

        Map<HashKey, HashKey> hashMap = ImmutableMap.of(k1, k1, k2, k2, k3, k3);
        assertThat(map.asMap(), equalTo(hashMap));

        map = map.assocAll(hashMap);
        assertThat(map.asMap(), equalTo(hashMap));

        map = map.dissoc(k1);
        assertThat(map.containsKey(k1), equalTo(false));
        assertThat(map.containsKey(k2), equalTo(true));
        assertThat(map.containsKey(k3), equalTo(true));

        map = map.dissoc(k2);
        map = map.dissoc(k2);
        assertThat(map.get(k2), nullValue());

        map = map.dissoc(k3);
        assertThat(map.get(k3), nullValue());

        assertThat(map.size(), equalTo(0));
    }

    @Test
    public void collisions_veto() {
        Merger<Map.Entry<HashKey, HashKey>> merger = PersistentHashMapTest.<HashKey, HashKey>vetoMerger();
        HashKey k1 = new HashKey(1);
        HashKey k2 = new HashKey(1);
        HashKey k3 = new HashKey(1);

        PersistentHashMap<HashKey, HashKey> map = PersistentHashMap.empty(),
                result;
        map = map.assoc(k1, k1);

        result = map.merge(k2, k2, merger);
        assertThat(result, sameInstance(map));
        assertThat(result.size(), equalTo(1));

        map = map.assoc(k2, k2);

        result = map.merge(k3, k3, merger);
        assertThat(result, sameInstance(map));
        assertThat(result.size(), equalTo(2));

        result = map.merge(k2, k3, merger);
        assertThat(result, sameInstance(map));
        assertThat(result.size(), equalTo(2));

        result = map.dissoc(k1, merger);
        assertThat(result, sameInstance(map));
        assertThat(result.size(), equalTo(2));
    }

    /**
     *
     */
    @Test
    public void Collisions_Incremental() {
        PersistentHashMap<HashKey, HashKey> map = PersistentHashMap.<HashKey, HashKey>empty();
        List<HashKey> keys = Lists.newArrayList();
        for (int i=0; i < 4097; i++) {
            HashKey key = new HashKey(i);
            keys.add(key);
            map = map.assoc(key, key);

            key = new HashKey(i);
            keys.add(key);
            map = map.assoc(key, key);
        }
        assertThat(map.size(), equalTo(keys.size()));
        for (HashKey key : keys) {
            assertThat(map.get(key), equalTo(key));
        }
        assertThat(map.get(new HashKey(5)), nullValue());

        int size = map.size();
        for (HashKey key : keys) {
            map = map.dissoc(key);
            map = map.dissoc(key);
            assertThat(map.size(), equalTo(size-1));
            size--;
        }
    }

    @Test
    public void Assoc_All_Map() {
        Map<Integer, Integer> ints = ImmutableMap.of(1, 1, 2, 2);
        Map<Integer, Integer> map = PersistentHashMap.copyOf(ints).asMap();
        assertThat(map, equalTo(ints));
    }

    @Test
    public void Assoc_All_PersistentMap() {
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.of(1, 1);
        PersistentHashMap<Integer, Integer> ints = PersistentHashMap.of(2, 2, 3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 1, 2, 2, 3, 3);

        assertThat(map.assocAll(ints).asMap(), equalTo(expected));
    }

    @Override
    protected PersistentHashMap<Integer, Integer> emptyMap() {
        return PersistentHashMap.empty();
    }

    @Test
    public void iterate_deepest_possible_tree() {
        int k1 = 0b00_11111_11111_11111_11111_11111_11111,
            k2 = 0b01_11111_11111_11111_11111_11111_11111,
            k3 = 0b10_11111_11111_11111_11111_11111_11111,
            k4 = 0b11_11111_11111_11111_11111_11111_11111;
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.<Integer, Integer>empty()
                .assoc(k1, 1)
                .assoc(k2, 2)
                .assoc(k3, 3)
                .assoc(k4, 4);
        Set<Integer> results = new HashSet<>();
        for (Iterator<Map.Entry<Integer, Integer>> iter = map.iterator(); iter.hasNext(); ) {
            Map.Entry<Integer, Integer> entry = iter.next();
            results.add(entry.getKey());
            results.add(entry.getValue());
        }
        assertThat(results, equalTo(ImmutableSet.of(k1, k2, k3, k4, 1, 2, 3, 4)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void assertMapProperties(PersistentMap<Integer, Integer> map) {
        assertThat(map, instanceOf(PersistentHashMap.class));
        PersistentHashMap<Integer, Integer> hashMap = (PersistentHashMap<Integer, Integer>) map;
        Node root = hashMap.root();
        if (root instanceof HashNode) {
            assertThat(((HashNode) root).updateContext.isCommitted(), equalTo(true));
        }
        if (root instanceof ArrayNode) {
            assertThat(((ArrayNode) root).updateContext.isCommitted(), equalTo(true));
        }
    }

    @Override
    protected void assertEmptyMap(PersistentMap<Integer, Integer> map) {
        assertThat(map.size(), equalTo(0));
        assertThat(((PersistentHashMap<Integer, Integer>) map).root(), notNullValue());
    }

}
