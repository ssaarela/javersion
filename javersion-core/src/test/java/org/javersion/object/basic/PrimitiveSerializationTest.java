package org.javersion.object.basic;

import static org.hamcrest.Matchers.equalTo;
import static org.javersion.object.basic.TestUtil.property;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.junit.Test;

import com.google.common.collect.Maps;

public class PrimitiveSerializationTest {

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
    
    private static final BasicObjectSerializer<Primitives> primitivesSerializer =
            new BasicObjectSerializer<>(Primitives.class);
    
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

        
        Map<PropertyPath, Object> properties = primitivesSerializer.toMap(primitives);
        
        Map<PropertyPath, Object> expectedProperties = getExpectedProperties(primitives);
        
        assertThat(properties, equalTo(expectedProperties));
    }
    
    @Test
    public void Primitive_Defaults() {
        Primitives primitives = new Primitives();
        
        Map<PropertyPath, Object> properties = primitivesSerializer.toMap(primitives);
        
        Map<PropertyPath, Object> expectedProperties = getExpectedProperties(primitives);
        
        assertThat(properties, equalTo(expectedProperties));
    }
    
    private static Map<PropertyPath, Object> getExpectedProperties(Object object) {
        Map<PropertyPath, Object> expectedProperties = Maps.newHashMap();
        expectedProperties.put(ROOT, object.getClass());
        
        for (FieldDescriptor field : TypeDescriptors.getTypeDescriptor(Primitives.class).getFields().values()) {
            expectedProperties.put(property(field.getName()), field.get(object));
        }
        return expectedProperties;
    }
}
