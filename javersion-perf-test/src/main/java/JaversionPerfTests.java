import java.util.Comparator;

import org.javersion.util.MapUpdate;
import org.javersion.util.MutableHashMap;
import org.javersion.util.PersistentHashMap;
import org.javersion.util.PersistentTreeMap;


public class JaversionPerfTests implements PerfTests<PersistentHashMap<Integer, Integer>, PersistentTreeMap<Integer, Integer>> {

    @Override
    public PersistentHashMap<Integer, Integer> incrementalInsert(Integer[] data) {
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.empty();
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return map;
    }

    @Override
    public void getAllByKeys(Integer[] data, PersistentHashMap<Integer, Integer> persistentMap) {
        for (int i=0; i < data.length; i++) {
            Object value = persistentMap.get(data[i]);
            if (!data[i].equals(value)) {
                throw new IllegalStateException(String.format("getAllByKeysJaversion: expected %s, got %s", data[i], value));
            }
        }
    }

    @Override
    public void incrementalDelete(Integer[] data, PersistentHashMap<Integer, Integer> persistentMap) {
        for (int i=0; i < data.length; i++) {
            persistentMap = persistentMap.dissoc(data[i]);
        }
    }

    @Override
    public PersistentHashMap<Integer, Integer> bulkInsert(final Integer[] data) {
        PersistentHashMap<Integer, Integer> map = PersistentHashMap.<Integer, Integer>empty().update(
                new MapUpdate<Integer, Integer>() {
                    @Override
                    public void apply(MutableHashMap<Integer, Integer> map) {
                        for (int i=0; i < data.length; i++) {
                            map.assoc(data[i], data[i]);
                        }
                    }
                });
        return map;
    }

    @Override
    public void bulkDelete(final Integer[] data, final PersistentHashMap<Integer, Integer> persistentMap) {
        int sizeAfterDelete = persistentMap.update(
                persistentMap.size(),
                        new MapUpdate<Integer, Integer>() {
                    @Override
                    public void apply(MutableHashMap<Integer, Integer> map) {
                        for (int i=0; i < data.length; i++) {
                            map.dissoc(data[i]);
                        }
                    }
                }).size();
        if (sizeAfterDelete != 0) {
            throw new AssertionError();
        }
    }

    @Override
    public PersistentTreeMap<Integer, Integer> sortedMapIncrementalInsert(Comparator<Integer> comparator, Integer[] data) {
        PersistentTreeMap<Integer, Integer> map = PersistentTreeMap.empty(comparator);
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return map;
    }

    @Override
    public void sortedMapIncrementalDelete(Integer[] data, PersistentTreeMap<Integer, Integer> sortedMap) {
        for (int i=0; i < data.length; i++) {
            sortedMap = sortedMap.dissoc(data[i]);
        }
    }

    @Override
    public String getImpl() {
        return "Javersion";
    }

    public static void main(String[] args) {
        TestPerformance.runTests(new JaversionPerfTests());
    }

}
