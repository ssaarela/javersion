package org.javersion.util;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

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
        PersistentMap<String, String> map = new PersistentMap<String, String>();
        assertThat(map.size(), equalTo(0));
        assertThat(map.containsKey("key"), equalTo(false));
        assertThat(map.iterator(), not(nullValue()));
        assertThat(map.iterator().hasNext(), equalTo(false));
    }

    @Test
    public void Add_Values() {
        PersistentMap<String, String> map = new PersistentMap<String, String>();
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
        
        PersistentMap<Object, Object> map = new PersistentMap<>();
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
        
        PersistentMap<Object, Object> map = new PersistentMap<>();
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
        HashKey k3 = new HashKey(0);

        PersistentMap<Object, Object> map = new PersistentMap<>();
        map = map.assoc(k0, k0);
        map = map.assoc(k1, k1);

        assertThat(map.dissoc(0).size(), equalTo(2));
        
        assertThat(map.dissoc(k1).size(), equalTo(1));
        assertThat(map.dissoc(k1).get(k0), equalTo((Object) k0));
        
        assertThat(map.dissoc(k0).size(), equalTo(1));
        assertThat(map.dissoc(k0).get(k0), nullValue());
        
        map = map.assoc(k3, k3);
        assertThat(map.dissoc(k1).size(), equalTo(2));
        assertThat(map.dissoc(k0).size(), equalTo(2));
        assertThat(map.dissoc(k0).get(k3), equalTo((Object) k3));
        assertThat(map.dissoc(k3).size(), equalTo(2));
        assertThat(map.dissoc(0), sameInstance(map));
    }
    
    @Test
    public void Collisions() {
        HashKey k1 = new HashKey(1);
        HashKey k2 = new HashKey(1);
        HashKey k3 = new HashKey(1);

        PersistentMap<HashKey, HashKey> map = new PersistentMap<>();
        map = map.assoc(k1, k1);
        map = map.assoc(k2, k1);
        map = map.assoc(k2, k2);
        map = map.assoc(k3, k3);
        
        assertThat(map.get(k1), equalTo(k1));
        assertThat(map.get(k2), equalTo(k2));
        assertThat(map.get(k3), equalTo(k3));
        
        assertThat(map.get(new HashKey(1)), nullValue());

        Map<HashKey, HashKey> hashMap = ImmutableMap.of(k1, k1, k2, k2, k3, k3);
        assertThat(map.atomicMap(), equalTo(hashMap));

        map = PersistentMap.<HashKey, HashKey>builder().putAll(hashMap).build();
        assertThat(map.atomicMap(), equalTo(hashMap));
        
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
    public void As_Map() {
        Random random = new Random(78);
        Map<Integer, Integer> hashMap = Maps.newHashMap();
        AtomicMap<Integer, Integer> map = new AtomicMap<Integer, Integer>();
        
        for (int i=1; i <= 257; i++) {
            Integer kv = random.nextInt();
            assertThat(map.put(kv, kv), equalTo(hashMap.put(kv, kv)));
        }
        hashMap.put(null, null);
        assertThat(map.put(null, null), nullValue());
 
        PersistentMap<Integer, Integer> persistentMap = map.getPersistentMap();
        
        assertThat(map, equalTo(hashMap));
        for (Integer key : map.keySet()) {
            assertThat(map.remove(key), equalTo(hashMap.remove(key)));
        }
        assertThat(map, equalTo(hashMap));
        
        assertThat(persistentMap.size(), equalTo(258)); // PersitentMap not modified
        
        map = persistentMap.atomicMap();
        map.clear();
        assertThat(map, equalTo(hashMap)); // empty
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
        PersistentMap<Integer, Integer> map = new PersistentMap<Integer, Integer>().assocAll(hashMap);
        assertThat(map.atomicMap(), equalTo(hashMap));
    }
    
    
    private static <KV> PersistentMap<KV, KV> incremental(List<KV> keys) {
        PersistentMap<KV, KV> persistentMap = new PersistentMap<KV, KV>();
        for (KV key : keys) {
            persistentMap = persistentMap.assoc(key, key);
        }
        return persistentMap;
    }

    
    
    private static final int LENGTH = 1000000;

    public static void main(String[] args) {
        Random random = new Random(78);
        String[] keys = new String[LENGTH * 2];
        HashMap<String, String> hashMap = new HashMap<>();
        PersistentMap<String, String> map = new PersistentMap<String, String>();
        for (int i=0; i < LENGTH; i++) {
            String key = Integer.toString(i);
            keys[i] = key;
            map = map.assoc(key, key); // warm up
            hashMap.put(key, key);
        }

        for (int i=LENGTH; i < LENGTH*2; i++) {
            String key = Integer.toString(random.nextInt(LENGTH * 2));
            keys[i] = key;
            map = map.assoc(key, key); // warm up
            hashMap.put(key, key);
        }

        long start;
        long elapsed; 

        // Bulk
        start = System.nanoTime();
        PersistentMap.Builder<String, String> builder = PersistentMap.builder();
        for (String key : keys) {
            builder.put(key, key);
        }
        map = builder.build();
        elapsed = System.nanoTime() - start;
        System.out.println("Bulk: " + elapsed / 1000000.0);
        
        // Verify
        start = System.nanoTime();
        verify(map, keys);
        if (map.size() != hashMap.size()) {
            throw new AssertionError();
        }
        elapsed = System.nanoTime() - start;
        System.out.println("Verify: " + elapsed / 1000000.0);
        
        // Incremental
        start = System.nanoTime();
        map = new PersistentMap<String, String>();
        for (String key : keys) {
            map = map.assoc(key, key);
        }
        elapsed = System.nanoTime() - start;
        System.out.println("Incremental: " + elapsed / 1000000.0);
        
        // Verify
        start = System.nanoTime();
        verify(map, keys);
        if (map.size() != hashMap.size()) {
            throw new AssertionError();
        }
        elapsed = System.nanoTime() - start;
        System.out.println("Verify: " + elapsed / 1000000.0);
        
        
        start = System.nanoTime();
        map.iterator();
        elapsed = System.nanoTime() - start;
        System.out.println("iterator(): " + elapsed / 1000000.0);
    }

    private static void verify(PersistentMap<String, String> map, String[] keys) {
        for (int i=0; i < keys.length; i++) {
            String key = keys[i];
            if (key != null && !key.equals(map.get(key))) {
                throw new AssertionError();
            }
        }
        if (map.containsKey(Integer.toString(-1))) {
            throw new AssertionError();
        }
        if (map.containsKey(Integer.toString(LENGTH*2))) {
            throw new AssertionError();
        }
    }

}
