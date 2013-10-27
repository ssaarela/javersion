package org.javersion.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.google.common.collect.Lists;

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
    public void Collision() {
        PersistentMap<HashKey, HashKey> map = new PersistentMap<>();
        HashKey k1 = new HashKey(1);
        HashKey k2 = new HashKey(1);
        HashKey k3 = new HashKey(1);
        map = map.assoc(k1, k1);
        map = map.assoc(k2, k2);
        map = map.assoc(k3, k3);
        assertThat(map.get(k1), equalTo(k1));
        assertThat(map.get(k2), equalTo(k2));
        assertThat(map.get(k3), equalTo(k3));
        
        assertThat(map.get(new HashKey(1)), nullValue());
    }
    
    @Test
    public void Incremental_Collisions() {
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
    }
    
    @Test
    public void Bulk_Collisions() {
        List<HashKey> keys = Lists.newArrayList();
        for (int i=0; i < 4097; i++) {
            keys.add(new HashKey(i));
            keys.add(new HashKey(i));
        }
        PersistentMap.Builder<HashKey, HashKey> builder = PersistentMap.builder();
        
        for (HashKey key : keys) {
            builder.put(key, key);
        }

        PersistentMap<HashKey, HashKey> map = builder.build();
        assertThat(map.size(), equalTo(keys.size()));
        for (HashKey key : keys) {
            assertThat(map.get(key), equalTo(key));
        }
        assertThat(map.get(new HashKey(5)), nullValue());
    }
    
    
    private static <KV> PersistentMap<KV, KV> incremental(List<KV> keys) {
        PersistentMap<KV, KV> persistentMap = new PersistentMap<KV, KV>();
        for (KV key : keys) {
            persistentMap = persistentMap.assoc(key, key);
        }
        return persistentMap;
    }

    public static <K, V> PersistentMap<K, V> incremental(Map<K, V> map) {
        PersistentMap<K, V> persistentMap = new PersistentMap<K, V>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            persistentMap = persistentMap.assoc(entry.getKey(), entry.getValue());
        }
        return persistentMap;
    }
    
    public PersistentMap<HashKey, HashKey> bulk(List<HashKey> keys) {
        PersistentMap<HashKey, HashKey> map = new PersistentMap<HashKey, HashKey>();
        for (HashKey key : keys) {
            map = map.assoc(key, key);
        }
        return map;
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
