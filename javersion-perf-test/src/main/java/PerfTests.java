import java.util.Comparator;


public interface PerfTests<PM, SM> {

    public PM incrementalInsert(Integer[] data);
    
    public void getAllByKeys(Integer[] data, PM persistentMap);
    
    public void incrementalDelete(Integer[] data, PM persistentMap);
    
    public PM bulkInsert(Integer[] data);
    
    public void bulkDelete(Integer[] data, PM persistentMap);
    
    public SM sortedMapIncrementalInsert(Comparator<Integer> comparator, Integer[] data);
    
    public void sortedMapIncrementalDelete(Integer[] data, SM sortedMap);
    
    public String getImpl();
    
}
