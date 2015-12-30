package org.javersion.object.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.object.mapping.ObjectTypeMapping.verifyAndSort;
import static org.javersion.reflect.TypeDescriptors.DEFAULT;

import java.util.*;

import org.javersion.reflect.TypeDescriptor;
import org.junit.Test;

public class ObjectTypeMappingTest {

    @Test
    public void topological_ordering() {
        assertOrder(
                SortedSet.class, Set.class, Collection.class,
                Collection.class, Set.class, SortedSet.class
        );
        assertOrder(
                Collection.class, SortedSet.class, Set.class,
                Collection.class, Set.class, SortedSet.class
        );
        assertOrder(
                Collection.class,
                Collection.class
        );
        assertOrder();

        assertOrder(
                List.class, SortedSet.class, Set.class, Collection.class, ArrayList.class, Object.class,
                Object.class, Collection.class, Set.class, SortedSet.class, List.class, ArrayList.class
        );
        assertOrder(
                Object.class, Collection.class, Set.class, SortedSet.class, List.class, ArrayList.class,
                Object.class, Collection.class, List.class, ArrayList.class, Set.class, SortedSet.class
        );
    }

    private void assertOrder(Class<?>... classes) {
        int mid = classes.length / 2;
        Class[] input = new Class[mid],
                expected = new Class[mid];
        System.arraycopy(classes, 0, input, 0, mid);
        System.arraycopy(classes, mid, expected, 0, mid);

        List result = Arrays.asList(verifyAndSort(mapOf(input)).values().stream()
                .map(TypeDescriptor::getRawType)
                .toArray());

        assertThat(result).isEqualTo(Arrays.asList(expected));
    }

    private Map<String, TypeDescriptor> mapOf(Class<?>... classes) {
        LinkedHashMap<String, TypeDescriptor> result = new LinkedHashMap<>();
        for (Class<?> cls : classes) {
            result.put(cls.getSimpleName(), DEFAULT.get(cls));
        }
        return result;
    }
}
