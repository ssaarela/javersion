package org.javersion.object;

import org.javersion.util.Check;

public class QueueItem<K, V> {
    public final K key;
    public final V value;
    public QueueItem(K key, V value) {
        this.key = Check.notNull(key, "key");
        this.value = value;
    }
    public boolean hasValue() {
        return value != null;
    }
}