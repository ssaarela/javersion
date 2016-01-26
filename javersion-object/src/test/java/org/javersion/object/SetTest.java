package org.javersion.object;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.of;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.javersion.core.Persistent.array;
import static org.javersion.path.PropertyPath.parse;
import static org.junit.Assert.assertThat;

import java.util.*;
import java.util.function.Function;

import org.javersion.core.Persistent;
import org.javersion.object.PolymorphismTest.Cat;
import org.javersion.object.PolymorphismTest.Pet;
import org.javersion.object.ReferencesTest.Node;
import org.javersion.path.PropertyPath;
import org.javersion.util.Check;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

public class SetTest {

    @Versionable
    public static class NodeSet {
        private Set<Node> nodes = Sets.newLinkedHashSet();
    }

    public static class NodeExt extends Node {
        Set<NodeExt> nodes = Sets.newLinkedHashSet();
        SortedSet<String> sorted = new TreeSet<>();
    }

    @Versionable
    public static class DoubleSet {
        Set<Double> doubles = new HashSet<>();
        Set<Float> floats = new HashSet<>();
    }


    @Versionable(alias = "MyComposite")
    @SetKey({ "first", "second", "third" })
    static class MyComposite {

        final int first;

        final String second;

        @VersionProperty("third")
        final int another;

        @SuppressWarnings("unused")
        private MyComposite() {
            first = 0;
            second = null;
            another = 0;
        }

        public MyComposite(int first, String second, int another) {
            this.first = first;
            this.second = second;
            this.another = another;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyComposite that = (MyComposite) o;

            if (first != that.first) return false;
            if (another != that.another) return false;
            return second != null ? second.equals(that.second) : that.second == null;

        }

        @Override
        public int hashCode() {
            int result = first;
            result = 31 * result + (second != null ? second.hashCode() : 0);
            result = 31 * result + another;
            return result;
        }
    }

    @Versionable
    static class MyCompositeContainerField {
        @SetKey({ "first", "second", "third" })
        Set<MyComposite> set = new HashSet<>();
    }

    @Versionable
    static class MyCompositeContainerType {
        Set<MyComposite> set = new HashSet<>();
    }

    @Versionable
    static class MyBadType {
        @Id
        public int id;
    }

    @Versionable
    static class MyBadTypeContainer {
        @SetKey("id")
        public Set<MyBadType> set;
    }

    @Versionable
    static class ReadOnlyId {
        int a;

        int b;

        private ReadOnlyId() {}

        public ReadOnlyId(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Id
        public int getId() {
            return a + b;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof ReadOnlyId) {
                ReadOnlyId other = (ReadOnlyId) obj;
                return getId() == other.getId();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getId();
        }
    }

    @Versionable
    static class ReadOnlyIdContainer {
        Set<ReadOnlyId> set;
    }

    @Versionable
    static class PetContainer {
        @SetKey("name")
        Set<Pet> set;
    }

    @SetKey(by = SetWrapper.Key.class)
    static class SetWrapper {
        final Set<Integer> set;

        SetWrapper(Integer... set) {
            this(copyOf(set));
        }
        @VersionCreator
        SetWrapper(Set<Integer> set) {
            this.set = Check.notNullOrEmpty(set, "set");
        }

        @VersionValue
        Set<Integer> toSet() {
            return set;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SetWrapper that = (SetWrapper) o;

            return set.equals(that.set);

        }

        @Override
        public int hashCode() {
            return set.hashCode();
        }

        static class Key implements Function<SetWrapper, Integer> {
            @Override
            public Integer apply(SetWrapper setWrapper) {
                return setWrapper.set.stream().reduce((a, b) -> a^b).get();
            }
        }
    }

    public static TypeMappings typeMappings = TypeMappings.builder()
            .withClass(Node.class)
            .havingSubClasses(NodeExt.class)
            .asReferenceForPath("nodes")
            .build();

    private final ObjectSerializer<NodeSet> nodeSetSerializer = new ObjectSerializer<>(NodeSet.class, typeMappings);

    private final ObjectSerializer<NodeExt> nodeExtSerializer = new ObjectSerializer<>(NodeExt.class, typeMappings);

    private final ObjectSerializer<DoubleSet> doubleSetSerializer = new ObjectSerializer<>(DoubleSet.class, typeMappings);

    private final MyComposite c1 = new MyComposite(1, "foo", 1);
    private final MyComposite c2 = new MyComposite(1, "bar", 1);
    private final MyComposite c3 = new MyComposite(2, "foo", 1);
    private final MyComposite c4 = new MyComposite(2, "foo", 2);

    @Test
    public void Write_And_Read_NodeSet() {
        NodeSet nodeSet = new NodeSet();
        Node node1 = new Node(123);
        Node node2 = new Node(456);
        node1.left = node1;
        node1.right = node2;
        node2.left = node1;
        node2.right = node2;

        nodeSet.nodes.add(node1);
        nodeSet.nodes.add(node2);

        Map<PropertyPath, Object> map = nodeSetSerializer.toPropertyMap(nodeSet);

        nodeSet = nodeSetSerializer.fromPropertyMap(map);
        assertThat(nodeSet.nodes, hasSize(2));
        Iterator<Node> iter = nodeSet.nodes.iterator();
        node1 = iter.next();
        node2 = iter.next();
        if (node1.id != 123) {
            Node tmp = node2;
            node2 = node1;
            node1 = tmp;
        }
        assertThat(node1.id, equalTo(123));
        assertThat(node2.id, equalTo(456));
        assertThat(node1.left, sameInstance(node1));
        assertThat(node1.right, sameInstance(node2));
        assertThat(node2.left, sameInstance(node1));
        assertThat(node2.right, sameInstance(node2));
    }

    @Test
    public void NodeExt_Containing_Itself_In_a_Set() {
        NodeExt nodeExt = new NodeExt();
        nodeExt.id = 789;
        nodeExt.nodes.add(nodeExt);
        nodeExt.sorted.add("omega");
        nodeExt.sorted.add("alpha");
        nodeExt.sorted.add("beta");

        Map<PropertyPath, Object> map = nodeExtSerializer.toPropertyMap(nodeExt);

        nodeExt = nodeExtSerializer.fromPropertyMap(map);
        assertThat(nodeExt.id, equalTo(789));
        assertThat(nodeExt.nodes, equalTo(singleton(nodeExt)));

        Iterator<String> iter = nodeExt.sorted.iterator();
        assertThat(iter.next(), equalTo("alpha"));
        assertThat(iter.next(), equalTo("beta"));
        assertThat(iter.next(), equalTo("omega"));
    }

    @Test
    public void double_set() {
        Set<Double> doubles = of(
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                1.1
        );
        Set<Float> floats = of(
                Float.NaN,
                Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                (float) 1.1
        );
        DoubleSet dset = new DoubleSet();
        dset.doubles = doubles;
        dset.floats = floats;

        dset = doubleSetSerializer.fromPropertyMap(doubleSetSerializer.toPropertyMap(dset));

        assertThat(dset.doubles, equalTo(doubles));
        assertThat(dset.floats, equalTo(floats));
    }

    @Test
    public void composite_id_field_annotation() {
        ObjectSerializer<MyCompositeContainerField> compositeSerializer = new ObjectSerializer<>(MyCompositeContainerField.class);
        MyCompositeContainerField container = new MyCompositeContainerField();
        container.set = getContainerSet();

        Map<PropertyPath, Object> properties = compositeSerializer.toPropertyMap(container);
        assertContainerProperties(properties);

        container = compositeSerializer.fromPropertyMap(properties);
        assertContainerSet(container.set);
    }

    @Test
    public void composite_id_type_annotation() {
        ObjectSerializer<MyCompositeContainerType> compositeSerializer = new ObjectSerializer<>(MyCompositeContainerType.class);
        MyCompositeContainerType container = new MyCompositeContainerType();
        container.set = getContainerSet();

        Map<PropertyPath, Object> properties = compositeSerializer.toPropertyMap(container);
        assertContainerProperties(properties);

        container = compositeSerializer.fromPropertyMap(properties);
        assertContainerSet(container.set);
    }

    @Test(expected = IllegalArgumentException.class)
    public void both_id_and_setKey_not_allowed() {
        new ObjectSerializer<>(MyBadTypeContainer.class);
    }

    @Test
    public void read_only_id() {
        ObjectSerializer<ReadOnlyIdContainer> serializer = new ObjectSerializer<ReadOnlyIdContainer>(ReadOnlyIdContainer.class);
        ReadOnlyId roi1 = new ReadOnlyId(1, 3);
        ReadOnlyId roi2 = new ReadOnlyId(2, 3);
        ReadOnlyIdContainer container = new ReadOnlyIdContainer();
        container.set = of(roi1, roi2);

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(container);
        assertThat(properties.keySet(), hasSize(8));
        assertThat(properties.get(parse("set[4].a")), equalTo(1l));
        assertThat(properties.get(parse("set[4].b")), equalTo(3l));
        assertThat(properties.get(parse("set[5].a")), equalTo(2l));
        assertThat(properties.get(parse("set[5].b")), equalTo(3l));

        container = serializer.fromPropertyMap(properties);

        assertThat(container.set, equalTo(of(roi2, roi1)));
    }

    @Test
    public void set_of_pets() {
        ObjectSerializer<PetContainer> serializer = new ObjectSerializer<>(PetContainer.class);
        PetContainer container = new PetContainer();
        container.set = of(new Cat("cat"));

        Map<PropertyPath, Object> properties = serializer.toPropertyMap(container);
        container = serializer.fromPropertyMap(properties);

        Pet pet = container.set.iterator().next();
        assertThat(pet, instanceOf(Cat.class));
        assertThat(pet.name, equalTo("cat"));
    }

    @Test
    public void functional_set_key() {
        final ImmutableSet<SetWrapper> input = of(
                new SetWrapper(1, 2),
                new SetWrapper(1, 3));

        ObjectSerializer<Set<SetWrapper>> serializer = new ObjectSerializer<>(new TypeToken<Set<SetWrapper>>() {});
        Map<PropertyPath, Object> properties = serializer.toPropertyMap(input);

        final Map<PropertyPath, Object> expected = ImmutableMap.<PropertyPath, Object>builder()
                .put(parse(""), array())
                .put(parse("[3]"), array())
                .put(parse("[3][1]"), 1l)
                .put(parse("[3][2]"), 2l)
                .put(parse("[2]"), array())
                .put(parse("[2][1]"), 1l)
                .put(parse("[2][3]"), 3l)
                .build();

        assertThat(properties, equalTo(expected));

        Set<SetWrapper> output = serializer.fromPropertyMap(properties);
        assertThat(output, equalTo(input));
    }

    private Set<MyComposite> getContainerSet() {
        return of(c1, c2, c3, c4);
    }

    private void assertContainerSet(Set<MyComposite> set) {
        assertThat(set, equalTo(of(c4, c3, c2, c1)));
    }

    private void assertContainerProperties(Map<PropertyPath, Object> properties) {
        assertThat(properties.keySet(), hasSize(18));
        assertThat(properties.get(parse("set[1][\"foo\"][1].first")), equalTo(1l));
        assertThat(properties.get(parse("set[1][\"bar\"][1].second")), equalTo("bar"));
        assertThat(properties.get(parse("set[2][\"foo\"][1]")), equalTo(Persistent.object("MyComposite")));
        assertThat(properties.get(parse("set[2][\"foo\"][2].third")), equalTo(2l));
    }
}
