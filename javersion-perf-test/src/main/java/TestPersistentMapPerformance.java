import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.javersion.util.MapUpdate;
import org.javersion.util.MutableMap;
import org.javersion.util.PersistentMap;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ITransientMap;
import clojure.lang.PersistentHashMap;

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

public class TestPersistentMapPerformance {
    private final int times; // = 1<<22; // 1
//    private final int length; // = 1<<5; // 1<<22
    private final String descriptor;
//    public static final int TIMES = 1;
//    public static final int LENGTH = 1<<22;

    private final Object[] data;// = new String[length];
    
    private static final Random RANDOM = new Random();
    
    private static Object[] sequentialData(int length) {
        Object[] data = new Object[length];
        for (int i=0; i < length; i++) {
            data[i] = Integer.toString(i);
        }
        return data;
    }
    private static Object[] randomData(int length) {
        Object[] data = new Object[length];
        for (int i=0; i < length; i++) {
            data[i] = Integer.toString(RANDOM.nextInt(length));
        }
        return data;
    }

//    private PersistentHashMap clojureMap = PersistentHashMap.EMPTY;
//    private PersistentMap<String, String> persistentMap = new PersistentMap<String, String>();
    
    private long start;
    private long elapsed; 
    
    
    
    public TestPersistentMapPerformance(String descriptor, Object[] data, int times) {
        this.descriptor = descriptor;
        this.data = data;
        this.times = times;
    }
    public TestPersistentMapPerformance warmup() {
        bulkInsertJaversion();
        bulkInsertClojure();
        return this;
    }
    
    public void run() {
        PersistentHashMap clojureMap = null;
        
        /**** CLOJURE ****/
        start();
        for (int i=0; i < times; i++)
            clojureMap = incrementalInsertClojure();
        end("incrementalInsert", "Clojure");
        
//        start();
//        for (int i=0; i < times; i++)
//            iterateAllClojure(clojureMap);
//        end("iterateAll", "Clojure");
        
        start();
        for (int i=0; i < times; i++)
            getAllByKeysClojure(clojureMap);
        end("getAllByKeys", "Clojure");
        
        start();
        for (int i=0; i < times; i++)
            incrementalDeleteClojure(clojureMap);
        end("incrementalDelete", "Clojure");
        clojureMap = null;

        
        start();
        for (int i=0; i < times; i++)
            clojureMap = bulkInsertClojure();
        end("bulkInsert", "Clojure");

        start();
        for (int i=0; i < times; i++)
            bulkDeleteClojure(clojureMap);
        end("bulkDelete", "Clojure");
        clojureMap = null;

        
        
        /**** JAVERSION ****/

        PersistentMap<Object, Object> javersionMap = null;
        start();
        for (int i=0; i < times; i++)
            javersionMap = incrementalInsertJaversion();
        end("incrementalInsert", "Javersion");
        
//        start();
//        for (int i=0; i < times; i++)
//            iterateAllJaversion(javersionMap);
//        end("iterateAll", "Javersion");
        
        start();
        for (int i=0; i < times; i++)
            getAllByKeysJaversion(javersionMap);
        end("getAllByKeys", "Javersion");
        
        start();
        for (int i=0; i < times; i++)
            incrementalDeleteJaversion(javersionMap);
        end("incrementalDelete", "Javersion");
        javersionMap = null;
        
        start();
        for (int i=0; i < times; i++)
            javersionMap = bulkInsertJaversion();
        end("bulkInsert", "Javersion");
        
        start();
        for (int i=0; i < times; i++)
            bulkDeleteJaversion(javersionMap);
        end("bulkDelete", "Javersion");
        javersionMap = null;
    }

//    private void iterateAllJaversion(PersistentMap<Object, Object> javersionMap) {
//        Iterator<Map.Entry<Object, Object>> iter = javersionMap.iterator();
//        while(iter.hasNext()) {
//            iter.next();
//        }
//    }
    private void getAllByKeysJaversion(PersistentMap<Object, Object> javersionMap) {
        for (int i=0; i < data.length; i++) {
            Object value = javersionMap.get(data[i]);
            if (!data[i].equals(value)) {
                throw new IllegalStateException(String.format("getAllByKeysJaversion: expected %s, got %s", data[i], value));
            }
        }
    }

//    private final IFn clojureReduce = new AFn() {
//        @Override
//        public Object invoke(Object arg1, Object arg2, Object arg3) {
//            return null;
//        }
//    };
//    private void iterateAllClojure(PersistentHashMap clojureMap) {
//        clojureMap.kvreduce(clojureReduce, null);
//    }
    
    private void getAllByKeysClojure(PersistentHashMap clojureMap) {
        for (int i=0; i < data.length; i++) {
            Object value = clojureMap.get(data[i]);
            if (!data[i].equals(value)) {
                throw new IllegalStateException(String.format("getAllByKeysClojure: expected %s, got %s", data[i], value));
            }
        }
    }
    private void start() {
        System.gc();
        start = System.nanoTime();
    }
    private void end(String title, String implementation) {
        elapsed = System.nanoTime() - start;
        System.out.println(String.format("{test:'%s',impl:'%s',nature:'%s',size:%s,repetitions:%s,time:%s},", title, implementation, descriptor, data.length, times, elapsed / 1000000.0));
    }
    private PersistentHashMap incrementalInsertClojure() {
        IPersistentMap map = PersistentHashMap.EMPTY;
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return (PersistentHashMap) map;
    }
    private PersistentMap<Object, Object> incrementalInsertJaversion() {
        PersistentMap<Object, Object> map = PersistentMap.empty();
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return map;
    }
    private void incrementalDeleteClojure(IPersistentMap map) {
        for (int i=0; i < data.length; i++) {
            map = map.without(data[i]);
        }
    }
    private void incrementalDeleteJaversion(PersistentMap<Object, Object> map) {
        for (int i=0; i < data.length; i++) {
            map = map.dissoc(data[i]);
        }
    }
    private void bulkDeleteClojure(PersistentHashMap persistentMap) {
        ITransientMap map = persistentMap.asTransient();
        for (int i=0; i < data.length; i++) {
            map = map.without(data[i]);
        }
        map.persistent();
    }
    private void bulkDeleteJaversion(final PersistentMap<Object, Object> persistentMap) {
        int sizeAfterDelete = persistentMap.update(
                persistentMap.size(),
                        new MapUpdate<Object, Object>() {
                    @Override
                    public void apply(MutableMap<Object, Object> map) {
                        for (int i=0; i < data.length; i++) {
                            map.dissoc(data[i]);
                        }
                    }
                }).size();
        if (sizeAfterDelete != 0) {
            throw new AssertionError();
        }
    }
    private PersistentHashMap bulkInsertClojure() {
        ITransientMap map = PersistentHashMap.EMPTY.asTransient();
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return (PersistentHashMap) map.persistent();
    }
    private PersistentMap<Object, Object> bulkInsertJaversion() {
        PersistentMap<Object, Object> map = PersistentMap.empty().update(
                new MapUpdate<Object, Object>() {
                    @Override
                    public void apply(MutableMap<Object, Object> map) {
                        for (int i=0; i < data.length; i++) {
                            map.assoc(data[i], data[i]);
                        }
                    }
                });
        return map;
    }
    public static void main(String[] args) {
        new TestPersistentMapPerformance("sequential", sequentialData(1<<21), 2).warmup().run();
        new TestPersistentMapPerformance("sequential", sequentialData(1<<19), 1<<4).run();
        new TestPersistentMapPerformance("sequential", sequentialData(1<<16), 1<<7).run();
        new TestPersistentMapPerformance("sequential", sequentialData(1<<13), 1<<10).run();
        new TestPersistentMapPerformance("sequential", sequentialData(1<<10), 1<<13).run();
        new TestPersistentMapPerformance("sequential", sequentialData(1<<7),  1<<16).run();
        new TestPersistentMapPerformance("sequential", sequentialData(1<<4),  1<<19).run();

        new TestPersistentMapPerformance("sequential", sequentialData(1<<5),  1<<22).run();

        new TestPersistentMapPerformance("random", randomData(1<<21), 2).run();
        new TestPersistentMapPerformance("random", randomData(1<<19), 1<<4).run();
        new TestPersistentMapPerformance("random", randomData(1<<16), 1<<7).run();
        new TestPersistentMapPerformance("random", randomData(1<<13), 1<<10).run();
        new TestPersistentMapPerformance("random", randomData(1<<10), 1<<13).run();
        new TestPersistentMapPerformance("random", randomData(1<<7),  1<<16).run();
        new TestPersistentMapPerformance("random", randomData(1<<4),  1<<19).run();

        new TestPersistentMapPerformance("random", randomData(1<<5),  1<<22).run();

        // For Profiling: 
//        try {
//            Thread.sleep(60000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        test.incrementalInsertJaversion();
//        System.out.println("1");
//        test.incrementalInsertJaversion();
//        System.out.println("2");
    }
}
