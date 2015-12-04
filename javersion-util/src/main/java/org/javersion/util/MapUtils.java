package org.javersion.util;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;

public class MapUtils {

    @SuppressWarnings("rawtypes")
    private static final Function GET_KEY = input -> input != null ? ((Entry) input).getKey() : null;

    @SuppressWarnings("rawtypes")
    private static final Function GET_VALUE = input -> input != null ? ((Entry) input).getValue() : null;

    @SuppressWarnings("unchecked")
    public static <K> Function<Map.Entry<K, ?>, K> mapKeyFunction() {
        return GET_KEY;
    };

    @SuppressWarnings("unchecked")
    public static <V> Function<Map.Entry<?, V>, V> mapValueFunction() {
        return GET_VALUE;
    };
}
