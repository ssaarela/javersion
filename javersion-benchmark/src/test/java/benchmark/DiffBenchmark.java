package org.javersion.benchmark;

import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.javersion.core.Diff;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.collect.Maps;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class DiffBenchmark {

    @Param({"10", "50", "100", "500", "1000", "5000"})
    public int size;

    private List<Integer> intsA;
    private List<Integer> intsB;

    private Map<Integer, Integer> hashMapA;
    private Map<Integer, Integer> hashMapB;

    private SortedMap<Integer, Integer> sortedMapA;
    private SortedMap<Integer, Integer> sortedMapB;

    @Setup
    public void setup() {
        intsA = range(0, size).boxed().collect(toList());
        shuffle(intsA, new Random(42));

        int mid = size / 2;
        intsB = range(mid, size+mid).boxed().collect(toList());
        shuffle(intsB, new Random(42));

        hashMapA = map(intsA);
        hashMapB = map(intsB);
        sortedMapA = sorted(intsA);
        sortedMapB = sorted(intsB);
    }

    @Benchmark
    public Map<Integer, Integer> mapDiff() {
        return Diff.diff(map(intsA), map(intsB));
    }

    @Benchmark
    public Map<Integer, Integer> sortedDiff() {
        return Diff.diff(sorted(intsA), sorted(intsB));
    }

    @Benchmark
    public Map<Integer, Integer> mapDiffOnly() {
        return Diff.diff(hashMapA, hashMapB);
    }

    @Benchmark
    public Map<Integer, Integer> sortedDiffOnly() {
        return Diff.diff(sortedMapA, sortedMapB);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DiffBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }


    public static <K> Map<K, K> map(List<K> keysAndValues) {
        if (keysAndValues.size() % 2 != 0) {
            throw new IllegalArgumentException("Expected even keysAndValues.size()");
        }
        Map<K, K> map = Maps.newHashMap();
        for (int i=0; i < keysAndValues.size(); i+=2) {
            map.put(keysAndValues.get(i), keysAndValues.get(i+1));
        }
        return map;
    }

    public static <K> SortedMap<K, K> sorted(List<K> keysAndValues) {
        if (keysAndValues.size() % 2 != 0) {
            throw new IllegalArgumentException("Expected even keysAndValues.size()");
        }
        SortedMap<K, K> map = new TreeMap<>();
        for (int i=0; i < keysAndValues.size(); i+=2) {
            map.put(keysAndValues.get(i), keysAndValues.get(i + 1));
        }
        return map;
    }
}
