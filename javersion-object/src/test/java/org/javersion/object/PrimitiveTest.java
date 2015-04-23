package org.javersion.object;

import static org.hamcrest.Matchers.equalTo;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.core.Persistent;
import org.javersion.path.PropertyPath;
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

        assertThat(properties.size(), equalTo(expectedProperties.size()));
        for (Map.Entry<PropertyPath, Object> entry : expectedProperties.entrySet()) {
            Object value = properties.get(entry.getKey());
            assertThat(value, equalTo(entry.getValue()));
        }
        primitives = primitivesSerializer.fromPropertyMap(properties);
        assertThat(getProperties(primitives), equalTo(expectedProperties));
    }

    @Test
    public void Primitive_Defaults() {
        assertPropertiesRoundTrip(new Primitives());
    }

    private static Map<PropertyPath, Object> getProperties(Primitives primitives) {
        Map<PropertyPath, Object> expectedProperties = Maps.newHashMap();
        expectedProperties.put(ROOT, Persistent.object("PrimitiveTest$Primitives"));
        expectedProperties.put(ROOT.property("string"), primitives.string);

        expectedProperties.put(ROOT.property("booleanPrimitive"), primitives.booleanPrimitive);
        expectedProperties.put(ROOT.property("booleanWrapper"), primitives.booleanWrapper);

        expectedProperties.put(ROOT.property("bytePrimitive"), (long) primitives.bytePrimitive);
        expectedProperties.put(ROOT.property("byteWrapper"), primitives.byteWrapper == null ? null : primitives.byteWrapper.longValue());

        expectedProperties.put(ROOT.property("shortPrimitive"), (long) primitives.shortPrimitive);
        expectedProperties.put(ROOT.property("shortWrapper"), primitives.shortWrapper == null ? null : primitives.shortWrapper.longValue());

        expectedProperties.put(ROOT.property("intPrimitive"), (long) primitives.intPrimitive);
        expectedProperties.put(ROOT.property("intWrapper"), primitives.intWrapper == null ? null : primitives.intWrapper.longValue());

        expectedProperties.put(ROOT.property("longPrimitive"), primitives.longPrimitive);
        expectedProperties.put(ROOT.property("longWrapper"), primitives.longWrapper == null ? null : primitives.longWrapper);

        expectedProperties.put(ROOT.property("floatPrimitive"), (double) primitives.floatPrimitive);
        expectedProperties.put(ROOT.property("floatWrapper"), primitives.floatWrapper == null ? null : primitives.floatWrapper.doubleValue());

        expectedProperties.put(ROOT.property("doublePrimitive"), primitives.doublePrimitive);
        expectedProperties.put(ROOT.property("doubleWrapper"), primitives.doubleWrapper == null ? null : primitives.doubleWrapper);

        expectedProperties.put(ROOT.property("charPrimitive"), Character.toString(primitives.charPrimitive));
        expectedProperties.put(ROOT.property("charWrapper"), primitives.charWrapper == null ? null : primitives.charWrapper.toString());

        return expectedProperties;
    }
}
