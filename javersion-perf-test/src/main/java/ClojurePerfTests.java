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

import clojure.lang.IPersistentMap;
import clojure.lang.ITransientMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentTreeMap;


public class ClojurePerfTests implements PerfTests<IPersistentMap, PersistentTreeMap> {

    @Override
    public PersistentHashMap incrementalInsert(Integer[] data) {
        IPersistentMap map = PersistentHashMap.EMPTY;
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return (PersistentHashMap) map;
    }

    @Override
    public void getAllByKeys(Integer[] data, IPersistentMap persistentMap) {
        for (int i=0; i < data.length; i++) {
            Object value = persistentMap.valAt(data[i]);
            if (!data[i].equals(value)) {
                throw new IllegalStateException(String.format("getAllByKeysClojure: expected %s, got %s", data[i], value));
            }
        }
    }

    @Override
    public void incrementalDelete(Integer[] data, IPersistentMap persistentMap) {
        for (int i=0; i < data.length; i++) {
            persistentMap = persistentMap.without(data[i]);
        }
    }

    @Override
    public IPersistentMap bulkInsert(Integer[] data) {
        ITransientMap map = PersistentHashMap.EMPTY.asTransient();
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return map.persistent();
    }

    @Override
    public void bulkDelete(Integer[] data, IPersistentMap persistentMap) {
        ITransientMap map = ((PersistentHashMap) persistentMap).asTransient();
        for (int i=0; i < data.length; i++) {
            map = map.without(data[i]);
        }
        map.persistent();
    }

    @Override
    public PersistentTreeMap sortedMapIncrementalInsert(Comparator<Integer> comparator, Integer[] data) {
        PersistentTreeMap map = new PersistentTreeMap(null, comparator);
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return map;
    }

    @Override
    public void sortedMapIncrementalDelete(Integer[] data, PersistentTreeMap sortedMap) {
        for (int i=0; i < data.length; i++) {
            sortedMap = sortedMap.without(data[i]);
        }
    }

    @Override
    public String getImpl() {
        return "Clojure";
    }

    public static void main(String[] args) {
        TestPerformance.runTests(new ClojurePerfTests());
    }

}
