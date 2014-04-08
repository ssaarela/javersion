import static java.lang.System.out;

import java.util.Random;
import java.util.TreeMap;

import org.javersion.util.MutableTreeMap;
import org.javersion.util.PersistentHashMap;
import org.javersion.util.PersistentTreeMap;

public class PerfTest {

    private static long start;
    
    public static void main(String[] args) {
        Integer[] data = randomData(4000);
        runJaversion(data);
//        runJaversionTree(data);
//        runJaversionMutableTree(data);

//        runClojure(data);
//        runClojureTree(data);

//        runJavaTree(data);
    }
    
    private static void runJaversion(Integer[] data) {
        out.println("Javersion PersistentHashMap");
        PersistentHashMap<Integer, Integer> jmap = PersistentHashMap.empty();
        // warmup
        for (Integer v : data) {
            jmap = jmap.assoc(v, v);
        }
        jmap = null;
        System.gc();

        long begin = System.nanoTime();
        for (int i=1; i <= data.length; i++) {
            start();
            jmap = PersistentHashMap.empty();
            for (int j=0; j < i; j++) {
                jmap = jmap.assoc(data[j], data[j]);
            }
            for (int j=0; j < i; j++) {
                jmap = jmap.dissoc(data[j]);
            }
            out.println(end());
        }
        out.println(String.format("\n%s", System.nanoTime() - begin));
    }
    
    private static void runClojure(Integer[] data) {
        out.println("Clojure PersistentHashMap");
        clojure.lang.PersistentHashMap cmap = clojure.lang.PersistentHashMap.EMPTY;

        // warmup
        for (Integer v : data) {
            cmap = (clojure.lang.PersistentHashMap) cmap.assoc(v, v);
        }
        cmap = null;
        System.gc();
        
        long begin = System.nanoTime();
        for (int i=1; i <= data.length; i++) {
            start();
            cmap = clojure.lang.PersistentHashMap.EMPTY;
            for (int j=0; j < i; j++) {
                cmap = (clojure.lang.PersistentHashMap) cmap.assoc(data[j], data[j]);
            }
            for (int j=0; j < i; j++) {
                cmap = (clojure.lang.PersistentHashMap) cmap.without(data[j]);
            }
            out.println(end());
        }
        out.println(String.format("\n%s", System.nanoTime() - begin));
    }
    
    private static void runJaversionTree(Integer[] data) {
        out.println("Javersion PersistentTreeMap");
        @SuppressWarnings("unchecked")
        PersistentTreeMap<Integer, Integer> jmap = PersistentTreeMap.EMPTY;
        // warmup
        for (Integer v : data) {
            jmap = jmap.assoc(v, v);
        }
        jmap = null;
//        System.gc();

        long begin = System.nanoTime();
        for (int i=1; i <= data.length; i++) {
            start();
            jmap = PersistentTreeMap.EMPTY;
            for (int j=0; j < i; j++) {
                jmap = jmap.assoc(data[j], data[j]);
            }
            for (int j=0; j < i; j++) {
                jmap = jmap.dissoc(data[j]);
            }
            out.println(end());
        }
        out.println(String.format("\nJaversion total time: %s", System.nanoTime() - begin));
    }
    
    private static void runJaversionMutableTree(Integer[] data) {
        out.println("Javersion MutableTreeMap");
        MutableTreeMap<Integer, Integer> jmap = new MutableTreeMap<>();
        // warmup
        for (Integer v : data) {
            jmap.put(v, v);
        }
        jmap = null;
//        System.gc();

        long begin = System.nanoTime();
        for (int i=1; i <= data.length; i++) {
            start();
            jmap = new MutableTreeMap<>();
            for (int j=0; j < i; j++) {
                jmap.put(data[j], data[j]);
            }
            for (int j=0; j < i; j++) {
                jmap.remove(data[j]);
            }
            out.println(end());
        }
        out.println(String.format("\n%s", System.nanoTime() - begin));
    }
    
    private static void runJavaTree(Integer[] data) {
        out.println("Java TreeMap");
        TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
        // warmup
        for (Integer v : data) {
            map.put(v, v);
        }
        map = null;
//        System.gc();

        long begin = System.nanoTime();
        for (int i=1; i <= data.length; i++) {
            start();
            map = new TreeMap<Integer, Integer>();
            for (int j=0; j < i; j++) {
                map.put(data[j], data[j]);
            }
            for (int j=0; j < i; j++) {
                map.remove(data[j]);
            }
            out.println(end());
        }
        out.println(String.format("\n%s", System.nanoTime() - begin));
    }
    
    private static void runClojureTree(Integer[] data) {
        out.println("Clojure PersistentTreeMap");
        clojure.lang.PersistentTreeMap cmap = clojure.lang.PersistentTreeMap.EMPTY;
        
        // warmup
        for (Integer v : data) {
            cmap = cmap.assoc(v, v);
        }
        cmap = null;
//        System.gc();

        long begin = System.nanoTime();
        for (int i=1; i <= data.length; i++) {
            start();
            cmap = clojure.lang.PersistentTreeMap.EMPTY;
            for (int j=0; j < i; j++) {
                cmap = cmap.assoc(data[j], data[j]);
            }
            for (int j=0; j < i; j++) {
                cmap = cmap.without(data[j]);
            }
            out.println(end());
        }
        out.println(String.format("\n%s", System.nanoTime() - begin));
    }

    private static void start() {
        start = System.nanoTime();
    }
    private static long end() {
        return System.nanoTime() - start;
    }
    
    private static final Random RANDOM = new Random();
    
    public static Integer[] sequentialData(int length) {
        Integer[] data = new Integer[length];
        for (int i=0; i < length; i++) {
            data[i] = i;
        }
        return data;
    }
    public static Integer[] randomData(int length) {
        Integer[] data = new Integer[length];
        for (int i=0; i < length; i++) {
            data[i] = RANDOM.nextInt(length);
        }
        return data;
    }

}
