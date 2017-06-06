package org.javersion.reflect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.reflect.TypeDescriptors.DEFAULT;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class BeanPropertyTest {

    static class MyBean {
        private String name;

        @SuppressWarnings("unused")
        private boolean readOnly;

        @SuppressWarnings("unused")
        private String writeOnly;

        @SuppressWarnings("unused")
        public boolean isReadOnly() {
            return readOnly;
        }

        @SuppressWarnings("unused")
        public void setWriteOnly(String writeOnly) {
            this.writeOnly = writeOnly;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIndexedProperty(int index) {
            return null;
        }

        public void setIndexedProperty(int index, String value) {}
    }

    static class MyGenericBean<T> {

        private T it;

        public T getIt() {
            return it;
        }

        public void setIt(T it) {
            this.it = it;
        }
    }

    private MyGenericBean<Integer> myGenericBean;

    @Test
    public void introspection() {
        Map<String, BeanProperty> properties = getProperties();
        assertThat(properties.keySet()).isEqualTo(ImmutableSet.of("name", "readOnly", "writeOnly"));
    }

    @Test
    public void readable_writable() {
        Map<String, BeanProperty> properties = getProperties();

        BeanProperty property = properties.get("name");
        assertThat(property.isReadable()).isTrue();
        assertThat(property.isWritable()).isTrue();

        property = properties.get("readOnly");
        assertThat(property.isReadable()).isTrue();
        assertThat(property.isWritable()).isFalse();

        property = properties.get("writeOnly");
        assertThat(property.isReadable()).isFalse();
        assertThat(property.isWritable()).isTrue();
    }

    @Test
    public void readable_from() {
        TypeDescriptor type = getTypeDescriptor();
        Map<String, BeanProperty> properties = getProperties();

        BeanProperty property = properties.get("name");
        assertThat(property.isReadableFrom(type)).isTrue();
        assertThat(property.isReadableFrom(DEFAULT.get(MethodDescriptorTest.class))).isFalse();
    }

    @Test
    public void writable_from() {
        TypeDescriptor type = getTypeDescriptor();
        Map<String, BeanProperty> properties = getProperties();

        BeanProperty property = properties.get("name");
        assertThat(property.isWritableFrom(type)).isTrue();
        assertThat(property.isWritableFrom(DEFAULT.get(MethodDescriptorTest.class))).isFalse();
    }

    @Test
    public void property_type() {
        BeanProperty property = getProperties().get("name");
        TypeDescriptor type = property.getType();
        assertThat(type.getRawType()).isEqualTo(String.class);

        property = getProperties().get("writeOnly");
        type = property.getType();
        assertThat(type.getRawType()).isEqualTo(String.class);
    }

    @Test
    public void declaring_type() {
        BeanProperty property = getProperties().get("name");
        TypeDescriptor type = property.getDeclaringType();
        assertThat(type.getRawType()).isEqualTo(MyBean.class);

        property = getProperties().get("writeOnly");
        type = property.getDeclaringType();
        assertThat(type.getRawType()).isEqualTo(MyBean.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_without_methods_is_not_allowed() {
        new BeanProperty("test", null, null);
    }

    @Test
    public void set_and_get() {
        BeanProperty property = getProperties().get("name");
        MyBean bean = new MyBean();

        property.set(bean, "MyBean");
        assertThat(bean.name).isEqualTo("MyBean");

        assertThat(property.get(bean)).isEqualTo("MyBean");
    }

    @Test
    public void get_methods() {
        TypeDescriptor type = getTypeDescriptor();
        Map<MethodSignature, MethodDescriptor> methods = type.getMethods();
        MethodDescriptor readMethod = methods.get(new MethodSignature("getName"));
        MethodDescriptor writeMethod = methods.get(new MethodSignature("setName", String.class));
        BeanProperty property = new BeanProperty("name", readMethod, writeMethod);

        assertThat(property.getReadMethod()).isSameAs(readMethod);
        assertThat(property.getWriteMethod()).isSameAs(writeMethod);
    }

    @Test
    public void generics() {
        FieldDescriptor genericField = DEFAULT.get(BeanPropertyTest.class).getField("myGenericBean");
        TypeDescriptor type = genericField.getType();
        BeanProperty property = type.getProperties().get("it");
        assertThat(property.getType().getRawType()).isEqualTo(Integer.class);
    }

    @Test
    public void skip_indexed_properties() {
        TypeDescriptor type = getTypeDescriptor();
        assertThat(type.getProperties()).doesNotContainKeys("indexedProperty");
    }

    private Map<String, BeanProperty> getProperties() {
        TypeDescriptor type = getTypeDescriptor();
        return type.getProperties();
    }

    private TypeDescriptor getTypeDescriptor() {
        return DEFAULT.get(MyBean.class);
    }
}
