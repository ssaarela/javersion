package org.javersion.object;

import static org.hamcrest.Matchers.equalTo;
import static org.javersion.object.TestUtil.property;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.path.PropertyPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.junit.Test;

import com.google.common.collect.Maps;

public class PrimitiveTest {

    @Versionable
    public static class Primitives {
        public String string;

        public Byte byteWrapper;
        public byte bytePrimitive;

        public Short shortWrapper;
        public short shortPrimitive;

        public Integer intWrapper;
        public int intPrimitive;

        public Long longWrapper;
        public long longPrimitive;

        public Float floatWrapper;
        public float floatPrimitive;

        public Double doubleWrapper;
        public double doublePrimitive;

        public Boolean booleanWrapper;
        public boolean booleanPrimitive;

        public Character charWrapper;
        public char charPrimitive;
    }

    private static final ObjectSerializer<Primitives> primitivesSerializer =
            new ObjectSerializer<>(Primitives.class);

    @Test
    public void Primitive_Values() {
        Primitives primitives = new Primitives();

        primitives.string = "String";

        primitives.bytePrimitive = (byte) 1;
        primitives.byteWrapper = primitives.bytePrimitive;

        primitives.shortPrimitive = (short) 2;
        primitives.shortWrapper = primitives.shortPrimitive;

        primitives.intPrimitive = 3;
        primitives.intWrapper = primitives.intPrimitive;

        primitives.longPrimitive = 4l;
        primitives.longWrapper = primitives.longPrimitive;

        primitives.floatPrimitive = 5.0f;
        primitives.floatWrapper = primitives.floatPrimitive;

        primitives.doublePrimitive = 6.0;
        primitives.doubleWrapper = primitives.doublePrimitive;

        primitives.booleanPrimitive = true;
        primitives.booleanWrapper = primitives.booleanPrimitive;

        primitives.charPrimitive = '7';
        primitives.charWrapper = primitives.charPrimitive;

        assertPropertiesRoundTrip(primitives);
    }

    private void assertPropertiesRoundTrip(Primitives primitives) {
        Map<PropertyPath, Object> properties = primitivesSerializer.toPropertyMap(primitives);
        Map<PropertyPath, Object> expectedProperties = getProperties(primitives);

        assertThat(properties, equalTo(expectedProperties));
        primitives = primitivesSerializer.fromPropertyMap(properties);
        assertThat(getProperties(primitives), equalTo(expectedProperties));
    }


    @Test
    public void Primitive_Defaults() {
        assertPropertiesRoundTrip(new Primitives());
    }

    private static Map<PropertyPath, Object> getProperties(Object object) {
        Map<PropertyPath, Object> expectedProperties = Maps.newHashMap();
        expectedProperties.put(ROOT, object.getClass());

        for (FieldDescriptor field : TypeDescriptors.getTypeDescriptor(Primitives.class).getFields().values()) {
            expectedProperties.put(property(field.getName()), field.get(object));
        }
        return expectedProperties;
    }
}
