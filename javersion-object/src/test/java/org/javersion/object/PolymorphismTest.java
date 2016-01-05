package org.javersion.object;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.javersion.object.Versionable.Subclass;
import org.javersion.path.PropertyPath;
import org.javersion.reflect.Param;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolymorphismTest {

    @Versionable(subclasses = {
            @Subclass(Dog.class),
            @Subclass(Cat.class)
    })
    public static class Pet {
        String name;
        @VersionConstructor
        public Pet(@Param("name") String baz) {
            this.name = baz;
        }
    }

    public static class Dog extends Pet {
        boolean bark = true;
        @JsonCreator
        public Dog(int number) {
            this(Integer.toString(number));
        }
        @VersionConstructor
        public Dog(@Param("name") String bar) {
            super(bar);
        }
    }

    public static class Cat extends Pet {
        boolean meow = true;
        @JsonCreator
        public Cat(@JsonProperty("name")  String foo) {
            super(foo);
        }
    }

    @Versionable
    public static class Owner {
        private Pet pet;
    }


    private TypeMappings typeMappings = TypeMappings.builder()
            .withClass(Pet.class)
            .havingSubClasses(Dog.class, Cat.class)
            .build();

    private final ObjectSerializer<Owner> serializer = new ObjectSerializer<>(Owner.class, typeMappings);

    @Test
    public void Write_And_Read_Owner_With_Dog() {
        Owner owner = new Owner();
        owner.pet = new Dog("Musti");

        Map<PropertyPath, Object> map = serializer.toPropertyMap(owner);

        owner = serializer.fromPropertyMap(map);
        assertThat(owner.pet, instanceOf(Dog.class));
        assertThat(owner.pet.name, equalTo("Musti"));
        assertThat(((Dog) owner.pet).bark, equalTo(true));
    }

    @Test
    public void Write_And_Read_Owner_With_Cat() {
        Owner owner = new Owner();
        owner.pet = new Cat("Mirri");

        Map<PropertyPath, Object> map = serializer.toPropertyMap(owner);

        owner = serializer.fromPropertyMap(map);
        assertThat(owner.pet, instanceOf(Cat.class));
        assertThat(owner.pet.name, equalTo("Mirri"));
        assertThat(((Cat) owner.pet).meow, equalTo(true));
    }
}
