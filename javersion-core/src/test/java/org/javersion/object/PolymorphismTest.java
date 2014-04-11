package org.javersion.object;

import java.util.Map;

import org.javersion.path.PropertyPath;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PolymorphismTest {

    public static class Pet {
        String name;
    }
    
    public static class Dog extends Pet {
        boolean bark = true;
    }
    
    public static class Cat extends Pet {
        boolean meow = true;
    }

    @Versionable
    public static class Owner {
        private Pet pet;
    }
    
    
    private ValueTypes valueTypes = ValueTypes.builder()
            .withClass(Pet.class)
            .havingSubClasses(Dog.class, Cat.class)
            .build();
    
    private final ObjectSerializer<Owner> serializer = new ObjectSerializer<>(Owner.class, valueTypes);
    
    @Test
    public void Write_And_Read_Owner_With_Dog() {
        Owner owner = new Owner();
        owner.pet = new Dog();
        owner.pet.name = "Musti";
        
        Map<PropertyPath, Object> map = serializer.toMap(owner);
        
        owner = serializer.fromMap(map);
        assertThat(owner.pet, instanceOf(Dog.class));
        assertThat(owner.pet.name, equalTo("Musti"));
        assertThat(((Dog) owner.pet).bark, equalTo(true));
    }
    
    @Test
    public void Write_And_Read_Owner_With_Cat() {
        Owner owner = new Owner();
        owner.pet = new Cat();
        owner.pet.name = "Mirri";
        
        Map<PropertyPath, Object> map = serializer.toMap(owner);
        
        owner = serializer.fromMap(map);
        assertThat(owner.pet, instanceOf(Cat.class));
        assertThat(owner.pet.name, equalTo("Mirri"));
        assertThat(((Cat) owner.pet).meow, equalTo(true));
    }

}
