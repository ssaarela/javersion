package org.javersion.reflect;

import com.google.common.reflect.TypeToken;
import org.junit.Test;

public class TypeDescriptorsTest {

    @Test(expected = ReflectionException.class)
    public void cache_exception() {
        TypeDescriptors.DEFAULT.get((TypeToken) null);
    }
}