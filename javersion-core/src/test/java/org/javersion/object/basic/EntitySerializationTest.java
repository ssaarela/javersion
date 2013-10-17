package org.javersion.object.basic;

import static org.hamcrest.Matchers.equalTo;
import static org.javersion.path.PropertyPath.ROOT;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.object.ValueMapping;
import org.javersion.object.Versionable;
import org.javersion.object.basic.BasicDescribeContext;
import org.javersion.object.basic.BasicSerializationContext;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.FieldDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.junit.Test;

import com.google.common.collect.Maps;

public class EntitySerializationTest {

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
    
    @Versionable
    public static class Cycle {
        public String name;
        public Cycle cycle;
        public Cycle(String name) {
            this.name = name;
        }
    }
    
    private static final ValueMapping<Object> primitivesValueMapping = BasicDescribeContext.describe(Primitives.class);
    
    private static final ValueMapping<Object> cycleValueMapping = BasicDescribeContext.describe(Cycle.class);
    
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

        
        BasicSerializationContext serializationContext = new BasicSerializationContext(primitivesValueMapping);
        serializationContext.serialize(primitives);
        
        Map<PropertyPath, Object> properties = serializationContext.getProperties();
        
        Map<PropertyPath, Object> expectedProperties = getExpectedProperties(primitives);
        
        assertThat(properties, equalTo(expectedProperties));
    }
    
    @Test
    public void Primitive_Defaults() {
        Primitives primitives = new Primitives();
        
        BasicSerializationContext serializationContext = new BasicSerializationContext(primitivesValueMapping);
        serializationContext.serialize(primitives);
        
        Map<PropertyPath, Object> properties = serializationContext.getProperties();
        
        Map<PropertyPath, Object> expectedProperties = getExpectedProperties(primitives);
        
        assertThat(properties, equalTo(expectedProperties));
    }
    
    @Test
    public void Hierarchy() {
        Cycle cycle;
        cycle = new Cycle("root");
        cycle.cycle = new Cycle("child");
        cycle.cycle.cycle = new Cycle("grandchild");
        
        BasicSerializationContext serializationContext = new BasicSerializationContext(cycleValueMapping);
        serializationContext.serialize(cycle);
        
        Map<PropertyPath, Object> properties = serializationContext.getProperties();
        
        Map<PropertyPath, Object> expectedProperties = properties(
                ROOT, Cycle.class,
                property("name"), "root",
                property("cycle"), Cycle.class,
        
                property("cycle.name"), "child",
                property("cycle.cycle"), Cycle.class,
        
                property("cycle.cycle.name"), "grandchild",
                property("cycle.cycle.cycle"), null
        );
        
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
    
    public static Map<PropertyPath, Object> properties(Object... keysAndValues) {
        Map<PropertyPath, Object> map = Maps.newHashMap();
        for (int i=0; i < keysAndValues.length-1; i+=2) {
            map.put((PropertyPath) keysAndValues[i], keysAndValues[i+1]);
        }
        return map;
    }
    
    private static PropertyPath property(String path) {
        return PropertyPath.parse(path);
    }
}
