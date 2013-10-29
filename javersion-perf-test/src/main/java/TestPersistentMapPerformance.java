import java.util.Random;

import org.javersion.util.PersistentMap;

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
    public static final int LENGTH = 1<<22;
    public static final String[] DATA = new String[LENGTH];
    static {
        for (int i=0; i < LENGTH/2; i++) {
            DATA[i] = Integer.toString(i);
        }
        
        Random random = new Random(78);
        for (int i=LENGTH; i < LENGTH; i++) {
            DATA[i] = Integer.toString(random.nextInt(LENGTH));
        }
    }

//    private PersistentHashMap clojureMap = PersistentHashMap.EMPTY;
//    private PersistentMap<String, String> persistentMap = new PersistentMap<String, String>();
    
    private long start;
    private long elapsed; 
    
    public void run() {
        // warmup
        incrementalInsertJaversion();
        incrementalInsertClojure();
        bulkInsertJaversion();
        bulkInsertClojure();

        start();
        PersistentHashMap clojureMap = incrementalInsertClojure();
        end("incrementalInsertClojure");
        
        start();
        incrementalDeleteClojure(clojureMap);
        end("incrementalDeleteClojure");
        clojureMap = null;
        

        start();
        PersistentMap<String, String> javersionMap = incrementalInsertJaversion();
        end("incrementalInsertJaversion");
        
        start();
        incrementalDeleteJaversion(javersionMap);
        end("incrementalDeleteJaversion");
        javersionMap = null;

        
        start();
        clojureMap = bulkInsertClojure();
        end("bulkInsertClojure");

        start();
        bulkDeleteClojure(clojureMap);
        end("bulkDeleteClojure");
        clojureMap = null;

        
        start();
        javersionMap = bulkInsertJaversion();
        end("bulkInsertJaversion");
        
        start();
        bulkDeleteJaversion(javersionMap);
        end("bulkDeleteJaversion");
        javersionMap = null;
    }
    private void start() {
        System.gc();
        start = System.nanoTime();
    }
    private void end(String title) {
        elapsed = System.nanoTime() - start;
        System.out.println(title + ": " + elapsed / 1000000.0);
    }
    private PersistentHashMap incrementalInsertClojure() {
        IPersistentMap map = PersistentHashMap.EMPTY;
        for (int i=0; i < LENGTH; i++) {
            map = map.assoc(DATA[i], DATA[i]);
        }
        return (PersistentHashMap) map;
    }
    private PersistentMap<String, String> incrementalInsertJaversion() {
        PersistentMap<String, String> map = new PersistentMap<String, String>();
        for (int i=0; i < LENGTH; i++) {
            map = map.assoc(DATA[i], DATA[i]);
        }
        return map;
    }
    private void incrementalDeleteClojure(IPersistentMap map) {
        for (int i=0; i < LENGTH; i++) {
            map = map.without(DATA[i]);
        }
    }
    private void incrementalDeleteJaversion(PersistentMap<String, String> map) {
        for (int i=0; i < LENGTH; i++) {
            map = map.dissoc(DATA[i]);
        }
    }
    private void bulkDeleteClojure(PersistentHashMap persistentMap) {
        ITransientMap map = persistentMap.asTransient();
        for (int i=0; i < LENGTH; i++) {
            map = map.without(DATA[i]);
        }
        map.persistent();
    }
    private void bulkDeleteJaversion(PersistentMap<String, String> persistentMap) {
        PersistentMap.Builder<String, String> map = PersistentMap.builder(persistentMap);
        for (int i=0; i < LENGTH; i++) {
            map = map.remove(DATA[i]);
        }
    }
    private PersistentHashMap bulkInsertClojure() {
        ITransientMap map = PersistentHashMap.EMPTY.asTransient();
        for (int i=0; i < LENGTH; i++) {
            map = map.assoc(DATA[i], DATA[i]);
        }
        return (PersistentHashMap) map.persistent();
    }
    private PersistentMap<String, String> bulkInsertJaversion() {
        PersistentMap.Builder<String, String> map = PersistentMap.builder();
        for (int i=0; i < LENGTH; i++) {
            map.put(DATA[i], DATA[i]);
        }
        return map.build();
    }
    public static void main(String[] args) {
        TestPersistentMapPerformance test = new TestPersistentMapPerformance();
        test.incrementalInsertJaversion();
        test.run();
        
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
//        test.incrementalInsertJaversion();
//        System.out.println("3");
//        test.incrementalInsertJaversion();
//        System.out.println("done");
    }
}
