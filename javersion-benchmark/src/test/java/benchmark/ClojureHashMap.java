package benchmark;

import static java.util.stream.IntStream.range;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.javersion.util.PersistentHashMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import clojure.lang.IPersistentMap;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class ClojureHashMap {

    @Param({"1", "2", "4", "8",
            "10", "20", "40", "80",
            "100", "200", "400", "800",
            "1000", "2000", "4000", "8000", })
    public int size;

    private PersistentHashMap<Integer, Integer> javersionMap;

    private IPersistentMap clojureMap;

    @Setup
    public void setup() {
        javersionMap = PersistentHashMap.empty();
        clojureMap = clojure.lang.PersistentHashMap.EMPTY;
        range(0, size).forEach(val -> {
            javersionMap = javersionMap.assoc(val, val);
            clojureMap = clojureMap.assoc(val, val);
        });
    }

    @Benchmark
    public void entryIteratorJaversion(Blackhole bh) {
        int count = 0;
        for (Iterator<Map.Entry<Integer, Integer>> iter = javersionMap.iterator(); iter.hasNext(); ) {
            bh.consume(iter.next());
            count++;
        }
        if (count != size) {
            throw new IllegalStateException("Illegal count!");
        }
    }

    @Benchmark
    public void entryIteratorClojure(Blackhole bh) {
        int count = 0;
        for (Iterator iter = clojureMap.iterator(); iter.hasNext(); ) {
            bh.consume((Map.Entry) iter.next());
            count++;
        }
        if (count != size) {
            throw new IllegalStateException("Illegal count!");
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ClojureHashMap.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
