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
import java.util.Comparator;

import org.javersion.util.MutableHashMap;
import org.javersion.util.MutableTreeMap;
import org.javersion.util.PersistentHashMap;
import org.javersion.util.PersistentTreeMap;

public class JaversionPerfTests implements PerfTests<PersistentHashMap<Integer, Integer>, PersistentTreeMap<Integer, Integer>> {

    @Override
    public PersistentHashMap<Integer, Integer> incrementalInsert(Integer[] data) {
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.empty();
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return map;
    }

    @Override
    public void getAllByKeys(Integer[] data, PersistentHashMap<Integer, Integer> persistentMap) {
        for (int i=0; i < data.length; i++) {
            Object value = persistentMap.get(data[i]);
            if (!data[i].equals(value)) {
                throw new IllegalStateException(String.format("getAllByKeysJaversion: expected %s, got %s", data[i], value));
            }
        }
    }

    @Override
    public void incrementalDelete(Integer[] data, PersistentHashMap<Integer, Integer> persistentMap) {
        for (int i=0; i < data.length; i++) {
            persistentMap = persistentMap.dissoc(data[i]);
        }
    }

    @Override
    public PersistentHashMap<Integer, Integer> bulkInsert(final Integer[] data) {
        MutableHashMap<Integer, Integer> mmap = new MutableHashMap<>();
        for (int i=0; i < data.length; i++) {
            mmap.put(data[i], data[i]);
        }
        return mmap.toPersistentMap();
    }

    @Override
    public void bulkDelete(final Integer[] data, final PersistentHashMap<Integer, Integer> persistentMap) {
        MutableHashMap<Integer, Integer> mmap = persistentMap.toMutableMap();
        for (int i=0; i < data.length; i++) {
            mmap.remove(data[i]);
        }
        if (mmap.size() != 0) {
            throw new AssertionError();
        }
    }

    @Override
    public PersistentTreeMap<Integer, Integer> sortedMapIncrementalInsert(Comparator<Integer> comparator, Integer[] data) {
        PersistentTreeMap<Integer, Integer> map = PersistentTreeMap.empty(comparator);
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return map;
    }

    @Override
    public PersistentTreeMap<Integer, Integer> sortedMapBulkInsert(Comparator<Integer> comparator, Integer[] data) {
        MutableTreeMap<Integer, Integer> map = new MutableTreeMap<>(comparator);
        for (int i=0; i < data.length; i++) {
            map.put(data[i], data[i]);
        }
        return map.toPersistentMap();
    }

    @Override
    public void sortedMapIncrementalDelete(Integer[] data, PersistentTreeMap<Integer, Integer> sortedMap) {
        for (int i=0; i < data.length; i++) {
            sortedMap = sortedMap.dissoc(data[i]);
        }
    }

    @Override
    public void sortedMapBulkDelete(Integer[] data, PersistentTreeMap<Integer, Integer> sortedMap) {
        MutableTreeMap<Integer, Integer> map = sortedMap.toMutableMap();
        for (int i=0; i < data.length; i++) {
            map.remove(data[i]);
        }
    }

    @Override
    public String getImpl() {
        return "Javersion";
    }

    public static void main(String[] args) {
        TestPerformance.runTests(new JaversionPerfTests());
    }

}
