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
import java.util.Random;

public class TestPerformance {

    private static final Comparator<Integer> COMPARATOR = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    };
    
    private static final Random RANDOM = new Random();
    
    private static Integer[] sequentialData(int length) {
        Integer[] data = new Integer[length];
        for (int i=0; i < length; i++) {
            data[i] = i;
        }
        return data;
    }
    private static Integer[] randomData(int length) {
        Integer[] data = new Integer[length];
        for (int i=0; i < length; i++) {
            data[i] = RANDOM.nextInt(length);
        }
        return data;
    }
    
    private static long start;
    private static long elapsed; 
    
    private static void start() {
        start = System.nanoTime();
    }
    private static void end(String title, String implementation, String nature, int size, int times) {
        elapsed = System.nanoTime() - start;
        System.out.println(String.format("{test:'%s',impl:'%s',nature:'%s',size:%s,repetitions:%s,time:%s},", title, implementation, nature, size, times, elapsed / 1000000.0));
    }

    public static <PM, SM> void runTestsFor(String nature, final Integer[] data, final int times, PerfTests<PM, SM> test) {
        PM persistentMap = null;
        SM sortedMap = null;
        start();
        for (int i=0; i < times; i++)
            persistentMap = test.incrementalInsert(data);
        end("incrementalInsert", test.getImpl(), nature, data.length, times);
        
        start();
        for (int i=0; i < times; i++)
            test.getAllByKeys(data, persistentMap);
        end("getAllByKeys", test.getImpl(), nature, data.length, times);
        
        start();
        for (int i=0; i < times; i++)
            test.incrementalDelete(data, persistentMap);
        end("incrementalDelete", test.getImpl(), nature, data.length, times);
        persistentMap = null;

        
        start();
        for (int i=0; i < times; i++)
            persistentMap = test.bulkInsert(data);
        end("bulkInsert", test.getImpl(), nature, data.length, times);

        start();
        for (int i=0; i < times; i++)
            test.bulkDelete(data, persistentMap);
        end("bulkDelete", test.getImpl(), nature, data.length, times);
        persistentMap = null;
        
        
        start();
        for (int i=0; i < times; i++)
            sortedMap = test.sortedMapIncrementalInsert(COMPARATOR, data);
        end("sortedMapIncrementalInsert", test.getImpl(), nature, data.length, times);

        start();
        for (int i=0; i < times; i++)
            test.sortedMapIncrementalDelete(data, sortedMap);
        end("sortedMapIncrementalDelete", test.getImpl(), nature, data.length, times);
        
        
        start();
        for (int i=0; i < times; i++)
            sortedMap = test.sortedMapBulkInsert(COMPARATOR, data);
        end("sortedMapBulkInsert", test.getImpl(), nature, data.length, times);

        start();
        for (int i=0; i < times; i++)
            test.sortedMapBulkDelete(data, sortedMap);
        end("sortedMapBulkDelete", test.getImpl(), nature, data.length, times);
    }

    public static void runTests(String nature, final Integer[] data, final int times, PerfTests<?, ?>... allTests) {
        for (PerfTests<?, ?> test : allTests) {
            runTestsFor(nature, data, times, test);
        }
    }
    
    public static <PM, SM> void warmupFor(Integer[] data, PerfTests<PM, SM> test) {
        test.incrementalInsert(data);
        test.getAllByKeys(data, test.bulkInsert(data));
        test.sortedMapIncrementalInsert(COMPARATOR, data);
    }
    
    public static void warmup(PerfTests<?, ?>... allTests) {
        Integer[] data = randomData(1234);
        for (PerfTests<?, ?> test : allTests) {
            warmupFor(data, test);
        }
    }
        
    public static <PM, SM> void runTests(PerfTests<?, ?>... allTests) {
        
        warmup(allTests);
        
        runTests("sequential", sequentialData(1<<21), 2, allTests);
        runTests("sequential", sequentialData(1<<19), 1<<4, allTests);
        runTests("sequential", sequentialData(1<<16), 1<<7, allTests);
        runTests("sequential", sequentialData(1<<13), 1<<10, allTests);
        runTests("sequential", sequentialData(1<<10), 1<<13, allTests);
        runTests("sequential", sequentialData(1<<7),  1<<16, allTests);
        runTests("sequential", sequentialData(1<<4),  1<<19, allTests);

        runTests("sequential", sequentialData(1<<5),  1<<22, allTests);

        runTests("random", randomData(1<<21), 2, allTests);
        runTests("random", randomData(1<<19), 1<<4, allTests);
        runTests("random", randomData(1<<16), 1<<7, allTests);
        runTests("random", randomData(1<<13), 1<<10, allTests);
        runTests("random", randomData(1<<10), 1<<13, allTests);
        runTests("random", randomData(1<<7),  1<<16, allTests);
        runTests("random", randomData(1<<4),  1<<19, allTests);

        runTests("random", randomData(1<<5),  1<<22, allTests);
    }
    public static void main(String[] args) {
        runTests(new JaversionPerfTests(), new ClojurePerfTests());
    }
}
