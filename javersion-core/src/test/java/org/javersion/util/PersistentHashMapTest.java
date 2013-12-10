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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PersistentHashMapTest {
    
    private static class HashKey {
        final int hash;
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
    
    @Test
    public void Empty_Map() {
        PersistentHashMap<String, String> map = PersistentHashMap.empty();
        assertThat(map.size(), equalTo(0));
        assertThat(map.containsKey("key"), equalTo(false));
        assertThat(map.iterator(), not(nullValue()));
        assertThat(map.iterator().hasNext(), equalTo(false));
    }

    @Test
    public void Add_Values() {
        PersistentHashMap<String, String> map = PersistentHashMap.empty();
        PersistentHashMap<String, String> otherMap = map.assoc("key", "value");
        assertThat(otherMap.get("key"), equalTo("value"));
        assertThat(map.get("key"), nullValue());

        map = otherMap.assoc("key", "value2");
        assertThat(map.get("key"), equalTo("value2"));
        assertThat(otherMap.get("key"), equalTo("value"));

        map = map.assoc("key2", "value");
        assertThat(map.get("key2"), equalTo("value"));
        assertThat(otherMap.get("key2"), nullValue());
        
        map = map.assoc("null", null);
        assertThat(map.get("null"), nullValue());
        assertThat(map.containsKey("null"), equalTo(true));
        
        assertThat(map.containsKey(null), equalTo(false));
        map = map.assoc(null, "not-null");
        assertThat(map.get(null), equalTo("not-null"));
    }
    
    @Test
    public void Size() {
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
    public void Collisions() {
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
    public void Collisions_Incremental() {
        List<HashKey> keys = Lists.newArrayList();
        for (int i=0; i < 4097; i++) {
            keys.add(new HashKey(i));
            keys.add(new HashKey(i));
        }
        PersistentHashMap<HashKey, HashKey> map = incremental(keys);
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
    public void Bulk() {
        Random random = new Random(87);
        Map<Integer, Integer> hashMap = Maps.newHashMap();
        for (int i=1; i <= 257; i++) {
            Integer kv = random.nextInt();
            hashMap.put(kv, kv);
        }
        hashMap.put(null, null);
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.copyOf(hashMap);
        assertThat(map.asMap(), equalTo(hashMap));
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void Merger_Gets_Called() {
        Merger<Entry<Integer, Integer>> merger = mock(Merger.class); 
        doReturn(true).when(merger).merge(any(Entry.class), any(Entry.class));
        ArgumentCaptor<Entry> entry1 = ArgumentCaptor.forClass(Entry.class);
        ArgumentCaptor<Entry> entry2 = ArgumentCaptor.forClass(Entry.class);

        PersistentHashMap<Integer, Integer> map = PersistentHashMap.empty();
        
        map = map.merge(1, 1, merger);
        assertThat(map.get(1), equalTo(1));
        verify(merger).insert(entry1.capture());
        assertEntry(entry1, 1, 1);
        
        map = map.merge(1, 2, merger);
        assertThat(map.get(1), equalTo(2));
        verify(merger).merge(entry1.capture(), entry2.capture());
        assertEntry(entry1, 1, 1);
        assertEntry(entry2, 1, 2);
        
        reset(merger);
        doReturn(true).when(merger).merge(any(Entry.class), any(Entry.class));
        map = map.merge(1, 2, merger);
        verify(merger).merge(entry1.capture(), entry2.capture());
        assertEntry(entry1, 1, 2);
        assertEntry(entry2, 1, 2);

        map = map.dissoc(1, merger);
        assertThat(map.get(1), nullValue());
        verify(merger).delete(entry2.capture());
        assertEntry(entry2, 1, 2);
    }
    
    @SuppressWarnings({ "rawtypes" })
    private void assertEntry(ArgumentCaptor<Entry> argument, Object key, Object value) {
        assertThat(argument.getValue().getKey(), equalTo(key));
        assertThat(argument.getValue().getValue(), equalTo(value));
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
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void Merge_All_Map() {
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.of(1, 1);
        Map<Integer, Integer> ints = ImmutableMap.of(1, 2, 3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 2, 3, 3);

        Merger<Entry<Integer, Integer>> merger = mock(Merger.class); 
        doReturn(true).when(merger).merge(any(Entry.class), any(Entry.class));
        
        map = map.mergeAll(ints, merger);
        
        assertThat(map.asMap(), equalTo(expected));
        
        ArgumentCaptor<Entry> entry1 = ArgumentCaptor.forClass(Entry.class);
        ArgumentCaptor<Entry> entry2 = ArgumentCaptor.forClass(Entry.class);
        
        verify(merger).merge(entry1.capture(), entry2.capture());
        assertEntry(entry1, 1, 1);
        assertEntry(entry2, 1, 2);
        
        verify(merger).insert(entry1.capture());
        assertEntry(entry1,3, 3);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void Merge_All_PersistentMap() {
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.of(1, 1);
        PersistentHashMap<Integer, Integer> ints = PersistentHashMap.of(1, 2, 3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 2, 3, 3);

        Merger<Entry<Integer, Integer>> merger = mock(Merger.class); 
        doReturn(true).when(merger).merge(any(Entry.class), any(Entry.class));
        
        map = map.mergeAll(ints, merger);
        
        assertThat(map.asMap(), equalTo(expected));
        
        ArgumentCaptor<Entry> entry1 = ArgumentCaptor.forClass(Entry.class);
        ArgumentCaptor<Entry> entry2 = ArgumentCaptor.forClass(Entry.class);

        verify(merger).merge(entry1.capture(), entry2.capture());
        assertEntry(entry1, 1, 1);
        assertEntry(entry2, 1, 2);
        
        verify(merger).insert(entry1.capture());
        assertEntry(entry1,3, 3);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void Merge_And_Keep_Old_Entry() {
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.of(1, 1);
        PersistentHashMap<Integer, Integer> ints = PersistentHashMap.of(1, 2, 3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 1, 3, 3);

        ArgumentCaptor<Entry> entry1 = ArgumentCaptor.forClass(Entry.class);
        ArgumentCaptor<Entry> entry2 = ArgumentCaptor.forClass(Entry.class);
        Merger<Entry<Integer, Integer>> merger = mock(Merger.class); 
        doReturn(false).when(merger).merge(any(Entry.class), any(Entry.class));
        
        map = map.mergeAll(ints, merger);
        
        assertThat(map.asMap(), equalTo(expected));

        assertThat(map.get(1), equalTo(1));
        
        verify(merger).merge(entry1.capture(), entry2.capture());
        assertEntry(entry1, 1, 1);
        assertEntry(entry2, 1, 2);
        
        verify(merger).insert(entry1.capture());
        assertEntry(entry1,3, 3);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void Immutability() {
        final int seed = new Random().nextInt();
        final Random random = new Random(seed);
        
        Integer[] ints = new Integer[3000];
        for (int i=0; i < ints.length; i++) {
            ints[i] = random.nextInt();
        }

        List<Map<Integer, Integer>> expectedMaps = new ArrayList<>(ints.length + 1);
        List<PersistentHashMap<Integer, Integer>> persistentMaps = new ArrayList<>(ints.length + 1);
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        PersistentHashMap<Integer, Integer> persistentMap = PersistentHashMap.of();
        expectedMaps.add(map);
        persistentMaps.add(persistentMap);
        
        for (Integer i : ints) {
            map = (Map<Integer, Integer>) ((HashMap<Integer, Integer>) map).clone();

            map.put(i, i);
            persistentMap = persistentMap.assoc(i, i);
            
            expectedMaps.add(map);
            persistentMaps.add(persistentMap);
        }

        assertEqualityOfMaps(seed, ints, expectedMaps, persistentMaps);

        for (int i=ints.length-1; i >= 0; i--) {
            persistentMap = persistentMap.dissoc(ints[i]);
            persistentMaps.set(i, persistentMap);
        }
        
        assertEqualityOfMaps(seed, ints, expectedMaps, persistentMaps);
    }

    private void assertEqualityOfMaps(final int seed, Integer[] ints,
            List<Map<Integer, Integer>> expectedMaps,
            List<PersistentHashMap<Integer, Integer>> persistentMaps)
            throws AssertionError {
        assertThat(persistentMaps.get(0).asMap(), equalTo(expectedMaps.get(0)));
        try {
            for (int i=0; i < ints.length; i++) {
                assertThat(persistentMaps.get(i+1).asMap(), equalTo(expectedMaps.get(i+1)));
            }
        } catch (Throwable e) {
            throw new AssertionError("Random(" + seed + "): " + e.getMessage(), e);
        }
    }
    
    @Test
    public void Editing_MutableMap_After_Committed_Doesnt_Affect_PersistedMap() {
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.of(1, 1);
        
        MutableHashMap<Integer, Integer> mutableMap = map.toMutableMap();
        assertThat(map.get(1), equalTo(1));
        
        // Editing 
        mutableMap.put(2, 2);
        assertThat(map.containsKey(2), equalTo(false));
        assertThat(mutableMap.containsKey(2), equalTo(true));
    }
    
    @Test(expected=IllegalStateException.class)
    public void Edit_MutableMap_From_Another_Thread() throws Throwable {
        final MutableHashMap<Integer, Integer> map = new MutableHashMap<>();
        final AtomicReference<Throwable> exception = new AtomicReference<>();

        final CountDownLatch countDown = new CountDownLatch(1);
        new Thread() {
            public void run() {
                // This should throw IllegalStateException!
                try {
                    map.put(1, 2);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    countDown.countDown();
                }
            }
        }.start();

        try {
            countDown.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertThat(exception.get(), notNullValue());
        throw exception.get();
    }
    
    private static <KV> PersistentHashMap<KV, KV> incremental(List<KV> keys) {
        PersistentHashMap<KV, KV> persistentMap = PersistentHashMap.empty();
        for (KV key : keys) {
            persistentMap = persistentMap.assoc(key, key);
        }
        return persistentMap;
    }

}
