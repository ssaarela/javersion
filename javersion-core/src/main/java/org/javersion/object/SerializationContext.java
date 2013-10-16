package org.javersion.object;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import com.google.common.collect.Maps;

public abstract class SerializationContext<V> {

    private static class QueueItem {
        public final PropertyPath path;
        public final Object object;
        public QueueItem(PropertyPath path, Object object) {
            this.path = path;
            this.object = object;
        }
        
    }
    private final ObjectDescriptor<V> rootDescriptor;
    
    private final Map<PropertyPath, V> properties = Maps.newHashMap();

    private final Deque<QueueItem> queue = new ArrayDeque<>();
    
    private QueueItem currentItem;
    
    public SerializationContext(ObjectDescriptor<V> rootDescriptor) {
        this.rootDescriptor = rootDescriptor;
    }
    
    public Object getCurrentObject() {
        return currentItem.object;
    }
    
    public PropertyPath getCurrentPath() {
        return currentItem.path;
    }
    
    public void serialize(Object object) {
        serialize(PropertyPath.ROOT, object);
        run();
    }
    
    public void put(V value) {
        put(getCurrentPath(), value);
    }
    
    public void put(PropertyPath path, V value) {
        if (properties.containsKey(path)) {
            throw new IllegalArgumentException("Duplicate value for " + path);
        }
        properties.put(path, value);
    }
    
    public void run() {
        while ((currentItem = queue.pollFirst()) != null) {
            ObjectDescriptor<V> descriptor = rootDescriptor.get(currentItem.path);
            descriptor.valueType.serialize(this);
        }
    }
    
    public void serialize(PropertyPath path, Object object) {
        queue.add(new QueueItem(path, object));
    }
    
}
