import java.util.Comparator;

import org.javersion.util.MapUpdate;
import org.javersion.util.MutableMap;
import org.javersion.util.PersistentMap;
import org.javersion.util.PersistentSortedMap;


public class JaversionPerfTests implements PerfTests<PersistentMap<Integer, Integer>, PersistentSortedMap<Integer, Integer>> {

    @Override
    public PersistentMap<Integer, Integer> incrementalInsert(Integer[] data) {
        PersistentMap<Integer, Integer> map = PersistentMap.empty();
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return map;
    }

    @Override
    public void getAllByKeys(Integer[] data, PersistentMap<Integer, Integer> persistentMap) {
        for (int i=0; i < data.length; i++) {
            Object value = persistentMap.get(data[i]);
            if (!data[i].equals(value)) {
                throw new IllegalStateException(String.format("getAllByKeysJaversion: expected %s, got %s", data[i], value));
            }
        }
    }

    @Override
    public void incrementalDelete(Integer[] data, PersistentMap<Integer, Integer> persistentMap) {
        for (int i=0; i < data.length; i++) {
            persistentMap = persistentMap.dissoc(data[i]);
        }
    }

    @Override
    public PersistentMap<Integer, Integer> bulkInsert(final Integer[] data) {
        PersistentMap<Integer, Integer> map = PersistentMap.<Integer, Integer>empty().update(
                new MapUpdate<Integer, Integer>() {
                    @Override
                    public void apply(MutableMap<Integer, Integer> map) {
                        for (int i=0; i < data.length; i++) {
                            map.assoc(data[i], data[i]);
                        }
                    }
                });
        return map;
    }

    @Override
    public void bulkDelete(final Integer[] data, final PersistentMap<Integer, Integer> persistentMap) {
        int sizeAfterDelete = persistentMap.update(
                persistentMap.size(),
                        new MapUpdate<Integer, Integer>() {
                    @Override
                    public void apply(MutableMap<Integer, Integer> map) {
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
    public PersistentSortedMap<Integer, Integer> sortedMapIncrementalInsert(Comparator<Integer> comparator, Integer[] data) {
        PersistentSortedMap<Integer, Integer> map = PersistentSortedMap.empty(comparator);
        for (int i=0; i < data.length; i++) {
            map = map.assoc(data[i], data[i]);
        }
        return map;
    }

    @Override
    public void sortedMapIncrementalDelete(Integer[] data, PersistentSortedMap<Integer, Integer> sortedMap) {
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
