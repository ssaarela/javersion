package org.javersion.object;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.javersion.object.PolymorphismTest.Cat;
import org.javersion.object.PolymorphismTest.Dog;
import org.javersion.object.PolymorphismTest.Pet;
import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ListTest {

    @Versionable
    public static class Owner {
        private List<Pet> pets;
    }

    private TypeMappings typeMappings = TypeMappings.builder()
            .withClass(Pet.class)
            .havingSubClasses(Dog.class, Cat.class)
            .build();

    private final ObjectSerializer<Owner> serializer = new ObjectSerializer<>(Owner.class, typeMappings);

    @Test
    public void Write_And_Read_Owner_With_Cats_And_Dogs() {
        Owner owner = new Owner();
        owner.pets = Lists.newArrayList(new Cat("Mirri"), null, new Dog("Musti"), null);
        Map<PropertyPath, Object> map = serializer.toPropertyMap(owner);

        owner = serializer.fromPropertyMap(map);
        // Trailing nulls are truncated!
        assertThat(owner.pets, hasSize(3));

        assertThat(owner.pets.get(0), instanceOf(Cat.class));
        assertThat(owner.pets.get(0).name, equalTo("Mirri"));
        assertThat(((Cat) owner.pets.get(0)).meow, equalTo(true));

        assertThat(owner.pets.get(1), nullValue());

        assertThat(owner.pets.get(2), instanceOf(Dog.class));
        assertThat(owner.pets.get(2).name, equalTo("Musti"));
        assertThat(((Dog) owner.pets.get(2)).bark, equalTo(true));
    }

    @Test
    public void large_sparse_list() {
        Pet[] pets = new Pet[1100];
        pets[0] = new Pet("0");
        pets[5] = new Pet("5");
        pets[10] = new Pet("10");
        pets[40] = new Pet("40");
        pets[100] = new Pet("100");
        pets[300] = new Pet("300");
        pets[1000] = new Pet("1000");

        Owner owner = new Owner();
        owner.pets = Arrays.asList(pets);
        assertThat(owner.pets, hasSize(1100));

        owner = serializer.fromPropertyMap(serializer.toPropertyMap(owner));

        assertThat(owner.pets, hasSize(1001));
        assertThat(owner.pets.get(0).name, equalTo("0"));
        assertThat(owner.pets.get(1), nullValue());
        assertThat(owner.pets.get(5).name, equalTo("5"));
        assertThat(owner.pets.get(9), nullValue());
        assertThat(owner.pets.get(10).name, equalTo("10"));
        assertThat(owner.pets.get(40).name, equalTo("40"));
        assertThat(owner.pets.get(100).name, equalTo("100"));
        assertThat(owner.pets.get(300).name, equalTo("300"));
        assertThat(owner.pets.get(1000).name, equalTo("1000"));
    }

}
