package benchmark;

import static org.javersion.core.Version.DEFAULT_BRANCH;
import static org.javersion.path.PropertyPath.ROOT;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.javersion.core.Merge;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.collect.ImmutableMap;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class MergeBenchmark {

    @Param({"1", "10", "20", "50", "100", "500"})
    public int versions;

    @Param({"100", "1000", "2000", "3000", "5000"})
    public int properties;

    private ObjectVersionGraph<Void> versionGraph;

    private Map<PropertyPath, Object> change;

    @Setup
    public void setup() {
        ImmutableMap.Builder<PropertyPath, Object> builder = ImmutableMap.builder();
        PropertyPath path = ROOT.property("list");
        for (int i=0; i < properties; i++) {
            builder.put(path.index(i), UUID.randomUUID().toString());
        }
        ObjectVersion<Void> version = ObjectVersion.<Void>builder().changeset(builder.build()).build();
        versionGraph = ObjectVersionGraph.init(version);

        builder.put(path.index(properties), UUID.randomUUID().toString());
        change = builder.build();
    }

    @Benchmark
    public Merge<PropertyPath, Object, Void> concurrentVersions() {
        ObjectVersion.Builder<Void> changeBuilder = ObjectVersion.<Void>builder()
                .parents(versionGraph.getTip().revision)
                .changeset(change);
        for (int i = 0; i < versions; i++) {
            versionGraph = versionGraph.commit(changeBuilder.build());
        }
        return versionGraph.mergeBranches(DEFAULT_BRANCH);
    }

    @Benchmark
    public Merge<PropertyPath, Object, Void> sequentialVersions() {
        ObjectVersion.Builder<Void> changeBuilder = ObjectVersion.<Void>builder()
                .changeset(change);
        for (int i = 0; i < versions; i++) {
            changeBuilder.parents(versionGraph.getTip().revision);
            versionGraph = versionGraph.commit(changeBuilder.build());
        }
        return versionGraph.mergeBranches(DEFAULT_BRANCH);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MergeBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
