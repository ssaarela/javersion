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


public interface PerfTests<PM, SM> {

    public PM incrementalInsert(Integer[] data);
    
    public void getAllByKeys(Integer[] data, PM persistentMap);
    
    public void incrementalDelete(Integer[] data, PM persistentMap);
    
    public PM bulkInsert(Integer[] data);
    
    public void bulkDelete(Integer[] data, PM persistentMap);
    
    public SM sortedMapIncrementalInsert(Comparator<Integer> comparator, Integer[] data);
    
    public SM sortedMapBulkInsert(Comparator<Integer> comparator, Integer[] data);
    
    public void sortedMapIncrementalDelete(Integer[] data, SM sortedMap);
    
    public void sortedMapBulkDelete(Integer[] data, SM sortedMap);
    
    public String getImpl();
    
}
