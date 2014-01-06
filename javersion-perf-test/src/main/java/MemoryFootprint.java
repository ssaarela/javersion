import static java.lang.System.out;

import java.io.PrintWriter;

import org.javersion.util.PersistentTreeSet;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.util.VMSupport;


public class MemoryFootprint {

    public static void main(String[] args) {
        out.println(VMSupport.vmDetails());

        PersistentTreeSet<Integer> jmap = PersistentTreeSet.empty();
        clojure.lang.PersistentTreeSet cmap = clojure.lang.PersistentTreeSet.EMPTY;
        
        footprint(PerfTest.randomData(50000), jmap, cmap);
    }
    
    private static void footprint(Integer[] data,
            PersistentTreeSet<Integer> jmap, 
            clojure.lang.IPersistentCollection cmap) {
        int i=0;
        for (Integer kv : data) {
            jmap = jmap.conj(kv);
            cmap = cmap.cons(kv);
            i++;
            if (i % 1000 == 0) {
                out.print(i);
                out.print('\t');
                out.print(GraphLayout.parseInstance(jmap).totalSize());
                out.print('\t');
                out.print(GraphLayout.parseInstance(cmap).totalSize());
                out.println();
            }
        }
        
        for (Integer kv : data) {
            if (cmap.count() != jmap.size()) {
                throw new AssertionError("Expected " + cmap.count() + " GOT " + jmap.size());
            }
        }
        
        PrintWriter pw = new PrintWriter(out);
        pw.println("Javersion");
        pw.println(GraphLayout.parseInstance(jmap).toFootprint());

        pw.println("Clojure");
        pw.println(GraphLayout.parseInstance(cmap).toFootprint());

        pw.close();
    }
}
