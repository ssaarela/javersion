package org.javersion.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.parse;
import static org.javersion.reflect.TypeDescriptor.getSimpleName;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javersion.core.Persistent;
import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class BeanTest {

    @Versionable
    static class MyBean {
        private int id;
        private String name;
        @VersionIgnore
        private String renamed;

        @Id
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }

        @VersionProperty
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        @VersionProperty("betterName")
        public String getRenamed() {
            return renamed;
        }
        public void setRenamed(String renamed) {
            this.renamed = renamed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MyBean)) return false;

            MyBean myBean = (MyBean) o;

            return id == myBean.id;

        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    @Versionable
    static class NonWritableProperty {

        protected String property;

        @VersionProperty
        public String getProperty() {
            return property;
        }
    }

    @Versionable
    static class WritablePropertyInSubclass extends NonWritableProperty {
        public void setProperty(String property) {
            this.property = property;
        }
    }

    @Versionable
    static class MyBeanSet {
        Set<MyBean> myBeans = new HashSet<>();
    }

    private ObjectSerializer<MyBean> serializer = new ObjectSerializer<>(MyBean.class);

    private ObjectSerializer<MyBeanSet> setSerializer = new ObjectSerializer<>(MyBeanSet.class);

    @Test
    public void read_write() {
        MyBean bean = new MyBean();
        bean.setId(123);
        bean.setName("MyBean");
        bean.setRenamed("Renamed property");

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(bean);
        assertThat(properties).isEqualTo(ImmutableMap.of(
                parse(""), Persistent.object(getSimpleName(MyBean.class)),
                parse("id"), Long.valueOf(bean.id),
                parse("name"), bean.name,
                parse("betterName"), bean.renamed
        ));

        MyBean other = serializer.fromPropertyMap(properties);
        assertThat(other.id).isEqualTo(bean.id);
        assertThat(other.name).isEqualTo(bean.name);
        assertThat(other.renamed).isEqualTo(bean.renamed);
    }

    @Test
    public void property_id() {
        MyBean bean = new MyBean();
        bean.id = 123;
        MyBeanSet beans = new MyBeanSet();
        beans.myBeans.add(bean);

        Map<PropertyPath, Object> properties = setSerializer.toPropertyMap(beans);
        beans = setSerializer.fromPropertyMap(properties);
        assertThat(beans.myBeans).isEqualTo(ImmutableSet.of(bean));
    }

    @Test
    public void getter_and_setter_in_different_class() {
        WritablePropertyInSubclass bean = new WritablePropertyInSubclass();
        bean.setProperty("property");
        ObjectSerializer<WritablePropertyInSubclass> serializer = new ObjectSerializer<>(WritablePropertyInSubclass.class);
        Map<PropertyPath, Object> properties = serializer.toPropertyMap(bean);
        bean = serializer.fromPropertyMap(properties);
        assertThat(bean.getProperty()).isEqualTo("property");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getter_and_setter_anomaly() {
        TypeMappings typeMappings = TypeMappings.builder()
                .withClass(NonWritableProperty.class, "NWP")
                .havingSubClass(WritablePropertyInSubclass.class, "WP")
                .build();
        new ObjectSerializer<>(NonWritableProperty.class, typeMappings);
    }
}
