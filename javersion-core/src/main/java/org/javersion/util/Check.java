package org.javersion.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Map;

public class Check {

    public static <T> T notNull(T reference, String fieldName) {
        return notNull$(reference, "%s should not be null", fieldName);
    }

    public static <T> T notNull$(T reference, String messageFormat, Object... args) {
        return checkNotNull(reference, messageFormat, args);
    }
    
    public static <T extends Iterable<?>> T notNullOrEmpty(T reference, String fieldName) {
        return notNullOrEmpty$(reference, "%s shoud not be null or empty. Got %s", fieldName, reference);
    }
    
    public static <T extends Iterable<?>> T notNullOrEmpty$(T reference, String messageFormat, Object... args) {
        checkArgument(!(reference == null || reference.iterator().hasNext()), messageFormat, args);
        return reference;
    }
    
    public static <T extends Collection<?>> T notNullOrEmpty(T reference, String fieldName) {
        return notNullOrEmpty$(reference, "%s shoud not be null or empty. Got %s", fieldName, reference);
    }
    
    public static <T extends Collection<?>> T notNullOrEmpty$(T reference, String messageFormat, Object... args) {
        checkArgument(!(reference == null || reference.size() > 0), messageFormat, args);
        return reference;
    }
    
    public static <K, V, T extends Map<K, V>> T notNullOrEmpty(T reference, String fieldName) {
        return notNullOrEmpty$(reference, "%s shoud not be null or empty. Got %s", fieldName, reference);
    }
    
    public static <K, V, T extends Map<K, V>> T notNullOrEmpty$(T reference, String messageFormat, Object... args) {
        checkArgument(!(reference == null || reference.size() > 0), messageFormat, args);
        return reference;
    }
    
    public static String notNullOrEmpty(String reference, String fieldName) {
        return notNullOrEmpty$(reference, "%s shoud not be null or empty. Got %s", fieldName, reference);
    }
    
    public static String notNullOrEmpty$(String reference, String messageFormat, Object... args) {
        checkArgument(!(reference == null || reference.length() == 0), messageFormat, args);
        return reference;
    }
    
}
