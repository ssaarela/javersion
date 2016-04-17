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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public abstract class AbstractPersistentMapTest<M extends PersistentMap<Integer, Integer>>
    extends AbstractCollectionTest {

    @Test
    public void Empty_Map() {
        PersistentMap<Integer, Integer> pmap = emptyMap();
        assertThat(pmap.size(), equalTo(0));
        assertThat(pmap.containsKey("key"), equalTo(false));
        assertThat(pmap.containsKey(1), equalTo(false));
        assertThat(pmap.iterator(), not(nullValue()));
        assertThat(pmap.iterator().hasNext(), equalTo(false));
        assertThat(pmap.dissoc(1), sameInstance(pmap));
        assertThat(pmap.asMap(), equalTo((Map<Integer, Integer>) Maps.<Integer, Integer>newHashMap()));
    }

    @Test
    public void Ascending() {
        assertInsertAndDelete(ascending(10));
    }

    @Test
    public void Ascending_Bulk_Insert() {
        assertBulkInsert(ascending(345));
    }

    private void assertBulkInsert(List<Integer> ints) {
        Map<Integer, Integer> map = Maps.newHashMapWithExpectedSize(ints.size());
        for (Integer kv : ints) {
            map.put(kv, kv);
        }
        PersistentMap<Integer, Integer> empty = emptyMap();
        PersistentMap<Integer, Integer> pmap = empty.assocAll(map);

        assertEmptyMap(empty);

        assertThat(pmap.asMap(), equalTo(map));

        for (Integer kv : ints) {
            assertThat(pmap.get(kv), equalTo(kv));
        }
        assertMapProperties(pmap);
    }

    @Test
    public void Descending_Bulk_Insert() {
        assertBulkInsert(descending(300));
    }

    @Test
    public void Descending() {
        assertInsertAndDelete(descending(300));
    }

    @Test
    public void Random() {
        try {
            assertInsertAndDelete(randoms(500));
        } catch (AssertionError e) {
            throw new AssertionError(DESC, e);
        }
    }

    protected abstract M emptyMap();

    @Test
    public void Re_Insertions() {
        List<Integer> ints = randoms(10);
        PersistentMap<Integer, Integer> map = emptyMap();
        for (int i=0; i < 3; i++) {
            for (Integer kv : ints) {
                map = map.assoc(kv, kv);
            }
        }
        assertThat(map.size(), equalTo(10));
        for (Integer kv : ints) {
            assertThat(map.get(kv), equalTo(kv));
        }
        assertMapProperties(map);
    }

    @Test
    public void Random_Bulk_Insert() {
        try {
            assertBulkInsert(randoms(300));
        } catch (AssertionError e) {
            throw new AssertionError(DESC, e);
        }
    }

    @Test
    public void Find_From_Empty_Map() {
        PersistentMap<Integer, Integer> map = emptyMap();
        assertThat(map.get(null), nullValue());
        assertThat(map.get(1), nullValue());
    }

    @Test
    public void Missing_Keys() {
        PersistentMap<Integer, Integer> pmap = emptyMap();
        for (int i=2; i < 100; i+=2) {
            Integer kv = i*5;
            pmap = pmap.assoc(kv, kv);
        }

        for (int i=1; i < 102; i+=2) {
            Integer kv = i*5;
            assertThat(pmap.get(kv), nullValue());
            assertThat(pmap.dissoc(kv), sameInstance(pmap));
        }
    }

    @Test(expected=NoSuchElementException.class)
    public void Iterate_Empty() {
        Iterator<Map.Entry<Integer, Integer>> iter = emptyMap().iterator();
        assertThat(iter.hasNext(), equalTo(false));
        iter.next();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void Merger_Gets_Called() {
        Merger<Entry<Integer, Integer>> merger = mock(Merger.class);
        doReturn(true).when(merger).insert(any(Entry.class));
        doReturn(true).when(merger).delete(any(Entry.class));
        doReturn(true).when(merger).merge(any(Entry.class), any(Entry.class));
        ArgumentCaptor<Entry> entry1 = ArgumentCaptor.forClass(Entry.class);
        ArgumentCaptor<Entry> entry2 = ArgumentCaptor.forClass(Entry.class);

        PersistentMap<Integer, Integer> map = emptyMap();

        map = map.merge(1, 1, merger);
        assertThat(map.get(1), equalTo(1));
        verify(merger).insert(entry1.capture());
        assertEntry(entry1, 1, 1);

        map = map.merge(1, 2, merger);
        assertThat(map.get(1), equalTo(2));
        verify(merger).merge(entry1.capture(), entry2.capture());
        assertEntry(entry1, 1, 1);
        assertEntry(entry2, 1, 2);

        map = map.dissoc(1, merger);
        assertThat(map.get(1), nullValue());
        verify(merger).delete(entry2.capture());
        assertEntry(entry2, 1, 2);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void Merge_All_Map() {
        PersistentMap<Integer, Integer> map = emptyMap().assoc(1, 1);
        Map<Integer, Integer> ints = ImmutableMap.of(1, 2, 3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 2, 3, 3);

        Merger<Entry<Integer, Integer>> merger = mock(Merger.class);
        doReturn(true).when(merger).insert(any(Entry.class));
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
        PersistentMap<Integer, Integer> map = emptyMap().assoc(1, 1);
        PersistentMap<Integer, Integer> ints = emptyMap().assoc(1, 2).assoc(3, 3);
        Map<Integer, Integer> expected = ImmutableMap.of(1, 1, 3, 3);

        ArgumentCaptor<Entry> entry1 = ArgumentCaptor.forClass(Entry.class);
        ArgumentCaptor<Entry> entry2 = ArgumentCaptor.forClass(Entry.class);
        Merger<Entry<Integer, Integer>> merger = mock(Merger.class);
        doReturn(true).when(merger).insert(any(Entry.class));
        doReturn(false).when(merger).merge(any(Entry.class), any(Entry.class));

        map = map.mergeAll(ints, merger);

        assertThat(map.asMap(), equalTo(expected));

        assertThat(map.get(1), equalTo(1));

        verify(merger).merge(entry1.capture(), entry2.capture());
        assertEntry(entry1, 1, 1);
        assertEntry(entry2, 1, 2);

        verify(merger).insert(entry1.capture());
        assertEntry(entry1, 3, 3);
    }

    @Test
    public void veto_insert() {
        final Merger<Entry<Integer, Integer>> merger = new Merger<Entry<Integer, Integer>>() {
            @Override
            public boolean insert(Entry<Integer, Integer> newEntry) {
                return false;
            }
        };
        PersistentMap<Integer, Integer> map = emptyMap();
        PersistentMap<Integer, Integer> result = map.merge(1, 1, merger);

        assertThat(result, sameInstance(map));
        assertThat(result.isEmpty(), equalTo(true));

        map = map.assoc(1, 1);
        result = map.merge(2, 2, merger);
        assertThat(result, sameInstance(map));
        assertThat(result.size(), equalTo(1));
    }

    @Test
    public void veto_delete() {
        PersistentMap<Integer, Integer> map = emptyMap().assoc(1, 1);
        PersistentMap<Integer, Integer> result = map.dissoc(1, new Merger<Entry<Integer, Integer>>() {
            @Override
            public boolean delete(Entry<Integer, Integer> newEntry) {
                return false;
            }
        });

        assertThat(result, sameInstance(map));
        assertThat(result.isEmpty(), equalTo(false));
    }

    @Test
    public void Editing_MutableMap_After_Committed_Doesnt_Affect_PersistedMap() {
        PersistentMap<Integer, Integer> map = emptyMap().assoc(1, 1);

        MutableMap<Integer, Integer> mutableMap = map.toMutableMap();
        assertThat(map.get(1), equalTo(1));

        // Editing
        mutableMap.put(2, 2);
        assertThat(map.containsKey(2), equalTo(false));
        assertThat(mutableMap.containsKey(2), equalTo(true));
    }

    @Test(expected=IllegalStateException.class)
    public void Edit_MutableMap_From_Another_Thread() throws Throwable {
        final MutableMap<Integer, Integer> map = emptyMap().toMutableMap();
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

    @Test
    public void assocAll() {
        PersistentMap<Integer, Integer> base = emptyMap().assoc(1, 1);
        PersistentMap<Integer, Integer> additional = emptyMap().assoc(2, 2).assoc(3, 3);
        PersistentMap<Integer, Integer> sum = base.assocAll(additional);
        assertThat(sum.size(), equalTo(3));
        assertThat(sum.get(1), equalTo(1));
        assertThat(sum.get(2), equalTo(2));
        assertThat(sum.get(3), equalTo(3));
    }

    @Test
    public void adding_duplicate_entry_returns_same_instance() {
        PersistentMap<Integer, Integer> map = emptyMap().assoc(1, 1);
        assertThat(map.assoc(1, 1), sameInstance(map));
    }

    @Test
    public void entry_spliterator() {
        PersistentMap<Integer, Integer> map = emptyMap();
        map.spliterator().tryAdvance(i-> {
            throw new RuntimeException();
        });

        map = map.assoc(1, 1).assoc(2, 2).assoc(3, 3).assoc(4, 4);
        Spliterator<Entry<Integer, Integer>> spliterator = map.spliterator();
        Spliterator<Entry<Integer, Integer>> split = spliterator.trySplit();

        MutableInt sum = new MutableInt(0);
        spliterator.forEachRemaining(entry -> sum.add(entry.getValue()));
        split.forEachRemaining(entry -> sum.add(entry.getValue()));
        assertThat(sum.intValue(), equalTo(10));
    }

    @Test
    public void value_spliterator() {
        PersistentMap<Integer, Integer> map = emptyMap();
        map.valueSpliterator().tryAdvance(i-> {
            throw new RuntimeException();
        });

        map = map.assoc(1, 1).assoc(2, 2).assoc(3, 3).assoc(4, 4);
        Spliterator<Integer> spliterator = map.valueSpliterator();
        Spliterator<Integer> split = spliterator.trySplit();

        MutableInt sum = new MutableInt(0);
        spliterator.forEachRemaining(value -> sum.add(value));
        split.forEachRemaining(value -> sum.add(value));
        assertThat(sum.intValue(), equalTo(10));
    }

    @Test
    public void key_spliterator() {
        PersistentMap<Integer, Integer> map = emptyMap();
        map.keySpliterator().tryAdvance(i-> {
            throw new RuntimeException();
        });

        map = map.assoc(1, 1).assoc(2, 2).assoc(3, 3).assoc(4, 4);
        Spliterator<Integer> spliterator = map.keySpliterator();
        Spliterator<Integer> split = spliterator.trySplit();

        MutableInt sum = new MutableInt(0);
        spliterator.forEachRemaining(value -> sum.add(value));
        split.forEachRemaining(value -> sum.add(value));
        assertThat(sum.intValue(), equalTo(10));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unmodifiable_entries() {
        PersistentMap<Integer, Integer> map = emptyMap().assoc(1, 1);
        map.iterator().next().setValue(2);
    }

    @Test
    public void iterate_empty() {
        assertThat(emptyMap().iterator().hasNext(), equalTo(false));
    }

    @Test(expected = NoSuchElementException.class)
    public void iterate_empty_no_such_element() {
        emptyMap().iterator().next();
    }

    @Test
    public void iterate_one() {
        Iterator<Entry<Integer, Integer>> iter = emptyMap().assoc(1, 2).iterator();
        assertThat(iter.hasNext(), equalTo(true));
        Entry<Integer, Integer> entry = iter.next();
        assertThat(entry.getKey(), equalTo(1));
        assertThat(entry.getValue(), equalTo(2));
    }

    @Test(expected = NoSuchElementException.class)
    public void iterate_one_no_such_element() {
        Iterator<Entry<Integer, Integer>> iter = emptyMap().assoc(1, 1).iterator();
        iter.next();
        iter.next();
    }

    @SuppressWarnings({ "rawtypes" })
    private void assertEntry(ArgumentCaptor<Entry> argument, Object key, Object value) {
        assertThat(argument.getValue().getKey(), equalTo(key));
        assertThat(argument.getValue().getValue(), equalTo(value));
    }

    protected void assertInsert(Integer... ints) {
        assertInsertAndDelete(Arrays.asList(ints));
    }

    protected void assertInsertAndDelete(List<Integer> ints) {
        PersistentMap<Integer, Integer> map = emptyMap();
        List<PersistentMap<Integer, Integer>> maps = new ArrayList<>(ints.size());
        for (Integer i : ints) {
            map = map.assoc(i, i);
            maps.add(map);
        }

        assertPersistentMaps(maps, ints);

        PersistentMap<Integer, Integer> map2 = map;
        for (Integer i : ints) {
            map2 = map2.dissoc(i);
            assertMapProperties(map2);
        }

        for (int i = ints.size() - 1; i > 0; i--) {
            Integer key = ints.get(i);
            map = map.dissoc(key);
            maps.set(i-1, map);
        }
        assertPersistentMaps(maps, ints);
    }

    protected void assertPersistentMaps(
            List<PersistentMap<Integer, Integer>> maps,
            List<Integer> ints) {
        for (int i=0; i < ints.size(); i++) {
            Map<Integer, Integer> map = Maps.newHashMap();
            PersistentMap<Integer, Integer> pmap = maps.get(i);
            assertMapProperties(pmap);
            assertThat(pmap.size(), equalTo(i+1));
            for (int j=0; j < ints.size(); j++) {
                Integer key = ints.get(j);
                // Contains all values of previous maps
                if (j <= i) {
                    assertThat(pmap.get(key), equalTo(key));
                    map.put(key, key);
                }
                // But none of the later values
                else {
                    assertThat(pmap.get(key), nullValue());
                }
            }
            assertThat(pmap.asMap(), equalTo(map));
        }
    }

    protected abstract void assertMapProperties(PersistentMap<Integer, Integer> map);

    protected abstract void assertEmptyMap(PersistentMap<Integer, Integer> map);

}
