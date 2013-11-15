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
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.javersion.util.AbstractTrieMap.Entry;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PersistentMapTest {
    
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
        PersistentMap<String, String> map = PersistentMap.empty();
        assertThat(map.size(), equalTo(0));
        assertThat(map.containsKey("key"), equalTo(false));
        assertThat(map.iterator(), not(nullValue()));
        assertThat(map.iterator().hasNext(), equalTo(false));
    }

    @Test
    public void Add_Values() {
        PersistentMap<String, String> map = PersistentMap.empty();
        PersistentMap<String, String> otherMap = map.assoc("key", "value");
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
        
        PersistentMap<Object, Object> map = PersistentMap.empty();
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
        
        PersistentMap<Object, Object> map = PersistentMap.empty();
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

        PersistentMap<Object, Object> map = PersistentMap.empty();
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

        PersistentMap<HashKey, HashKey> map = PersistentMap.empty();
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
        PersistentMap<HashKey, HashKey> map = incremental(keys);
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
        PersistentMap<Integer, Integer> map = PersistentMap.copyOf(hashMap);
        assertThat(map.asMap(), equalTo(hashMap));
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void Merger_Gets_Called() {
        Merger<Entry<Integer, Integer>> merger = mock(Merger.class); 
        doReturn(new Entry(1, 2)).when(merger).merge(any(Entry.class), any(Entry.class));

        PersistentMap<Integer, Integer> map = PersistentMap.empty();
        
        map = map.merge(1, 1, merger);
        assertThat(map.get(1), equalTo(1));
        
        map = map.merge(1, 2, merger);
        assertThat(map.get(1), equalTo(2));

        map = map.dissoc(1, merger);
        assertThat(map.get(1), nullValue());

        ArgumentCaptor<Entry> entry1 = ArgumentCaptor.forClass(Entry.class);
        ArgumentCaptor<Entry> entry2 = ArgumentCaptor.forClass(Entry.class);

        verify(merger).insert(entry1.capture());
        assertEntry(entry1, 1, 1);

        verify(merger).merge(entry1.capture(), entry2.capture());
        assertEntry(entry1, 1, 1);
        assertEntry(entry2, 1, 2);

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
        Map<Integer, Integer> map = PersistentMap.copyOf(ints).asMap();
        assertThat(map, equalTo(ints));
    }
    
    @Test
    public void Assoc_All_PersistentMap() {
        PersistentMap<Integer, Integer> map = PersistentMap.of(1, 1);
        PersistentMap<Integer, Integer> ints = PersistentMap.of(2, 2, 3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 1, 2, 2, 3, 3);

        assertThat(map.assocAll(ints).asMap(), equalTo(expected));
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void Merge_All_Map() {
        PersistentMap<Integer, Integer> map = PersistentMap.of(1, 1);
        Map<Integer, Integer> ints = ImmutableMap.of(1, 2, 3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 2, 3, 3);

        Merger<Entry<Integer, Integer>> merger = mock(Merger.class); 
        doReturn(new Entry(1, 2)).when(merger).merge(any(Entry.class), any(Entry.class));
        
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
        PersistentMap<Integer, Integer> map = PersistentMap.of(1, 1);
        PersistentMap<Integer, Integer> ints = PersistentMap.of(1, 2, 3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 2, 3, 3);

        Merger<Entry<Integer, Integer>> merger = mock(Merger.class); 
        doReturn(new Entry(1, 2)).when(merger).merge(any(Entry.class), any(Entry.class));
        
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
        PersistentMap<Integer, Integer> map = PersistentMap.of(1, 1);
        PersistentMap<Integer, Integer> ints = PersistentMap.of(1, 2, 3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 1, 3, 3);

        ArgumentCaptor<Entry> entry1 = ArgumentCaptor.forClass(Entry.class);
        ArgumentCaptor<Entry> entry2 = ArgumentCaptor.forClass(Entry.class);
        Merger<Entry<Integer, Integer>> merger = mock(Merger.class); 
        doReturn(new Entry(1, 1)).when(merger).merge(any(Entry.class), any(Entry.class));
        
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
        List<PersistentMap<Integer, Integer>> persistentMaps = new ArrayList<>(ints.length + 1);
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        PersistentMap<Integer, Integer> persistentMap = PersistentMap.of();
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
            List<PersistentMap<Integer, Integer>> persistentMaps)
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
        PersistentMap<Integer, Integer> map = PersistentMap.empty();
        final AtomicReference<MutableMap<Integer, Integer>> mutableMapRef = new AtomicReference<>();
        map = map.update(new MapUpdate<Integer, Integer>() {
            
            @Override
            public void apply(MutableMap<Integer, Integer> map) {
                map.assoc(1, 1);
                mutableMapRef.set(map);
            }
        });
        assertThat(map.get(1), equalTo(1));
        // Editing 
        mutableMapRef.get().assoc(2, 2);
        assertThat(map.containsKey(2), equalTo(false));
        assertThat(mutableMapRef.get().containsKey(2), equalTo(true));
    }
    
    @Test(expected=IllegalStateException.class)
    public void Edit_MutableMap_From_Another_Thread() throws Throwable {
        PersistentMap<Integer, Integer> map = PersistentMap.empty();
        final AtomicReference<Throwable> exception = new AtomicReference<>();
        map.update(new MapUpdate<Integer, Integer>() {
            @Override
            public void apply(final MutableMap<Integer, Integer> map) {
                final CountDownLatch countDown = new CountDownLatch(1);
                new Thread() {
                    public void run() {
                        // This should throw IllegalStateException!
                        try {
                            map.assoc(1, 2);
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
            }
        });
        assertThat(exception.get(), notNullValue());
        throw exception.get();
    }
    
    private static <KV> PersistentMap<KV, KV> incremental(List<KV> keys) {
        PersistentMap<KV, KV> persistentMap = PersistentMap.empty();
        for (KV key : keys) {
            persistentMap = persistentMap.assoc(key, key);
        }
        return persistentMap;
    }

}
