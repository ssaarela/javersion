package org.javersion.object;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.javersion.object.PolymorphismTest.Pet;
import org.javersion.path.PropertyPath;
import org.junit.Test;

import com.google.common.collect.Lists;

public class CollectionTest {

    @Versionable
    public static class Owner {
        private Collection<Pet> pets;
    }

    private final ObjectSerializer<Owner> serializer = new ObjectSerializer<>(Owner.class, TypeMappings.builder()
            .withClass(Pet.class).build());

    @Test
    public void Write_And_Read_Owner_With_Pets() {
        Owner owner = new Owner();
        owner.pets = Lists.newArrayList(new Pet("Mirri"), null, new Pet("Musti"));
        Map<PropertyPath, Object> map = serializer.toPropertyMap(owner);

        owner = serializer.fromPropertyMap(map);
        assertThat(owner.pets, hasSize(3));

        Iterator<Pet> iter = owner.pets.iterator();
        assertThat(iter.next().name, equalTo("Mirri"));

        assertThat(iter.next(), nullValue());

        assertThat(iter.next().name, equalTo("Musti"));
    }

}
