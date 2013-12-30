import static java.lang.System.out;

import java.io.PrintWriter;

import org.javersion.util.PersistentHashMap;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.util.VMSupport;


public class MemoryFootprint {

    public static void main(String[] args) {
        out.println(VMSupport.vmDetails());

        PersistentHashMap<Integer, Integer> jmap = PersistentHashMap.empty();
        clojure.lang.PersistentHashMap cmap = clojure.lang.PersistentHashMap.EMPTY;
        
        footprint(TestPerformance.randomData(50000), jmap, cmap);
    }
    
    private static void footprint(Integer[] data,
            PersistentHashMap<Integer, Integer> jmap, 
            clojure.lang.IPersistentMap cmap) {
        int i=0;
        for (Integer kv : data) {
            jmap = jmap.assoc(kv, kv);
            cmap = cmap.assoc(kv, kv);
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
        PrintWriter pw = new PrintWriter(out);
        pw.println("Javersion");
        pw.println(GraphLayout.parseInstance(jmap).toFootprint());

        pw.println("Clojure");
        pw.println(GraphLayout.parseInstance(cmap).toFootprint());

        pw.close();
    }
}
