package org.javersion.json;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.math.BigDecimal;
import java.util.Map;

import org.javersion.object.Persistent;
import org.javersion.object.types.ListType;
import org.javersion.object.types.MapType;

import com.google.common.collect.ImmutableMap;

public enum JsonType {
    OBJECT,
    ARRAY,
    NUMBER,
    BOOLEAN,
    STRING,
    NULL;

    private static final Map<Class<?>, JsonType> PERSISTENT_TO_JSON_TYPE;

    static {
        ImmutableMap.Builder builder = ImmutableMap.builder();

        builder.put(Persistent.Object.class, OBJECT);
        builder.put(Persistent.Array.class, ARRAY);
        builder.put(String.class, STRING);
        builder.put(Boolean.class, BOOLEAN);
        builder.put(Long.class, NUMBER);
        builder.put(Double.class, NUMBER);
        builder.put(BigDecimal.class, NUMBER);

        PERSISTENT_TO_JSON_TYPE = builder.build();
    }

    public static JsonType getType(Object value) {
        if (value == null) {
            return NULL;
        }
        return PERSISTENT_TO_JSON_TYPE.get(value.getClass());
    }
}
