package org.javersion.core;

public class VersionProperty<V> {

    public final long revision;
    
    public final V value;

    public VersionProperty(long revision, V value) {
        this.revision = revision;
        this.value = value;
    }
    
    public String toString() {
        return String.valueOf(value) + "#" + revision;
    }
}
