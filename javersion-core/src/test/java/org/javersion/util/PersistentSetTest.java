package org.javersion.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Random;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

public class PersistentSetTest {

    public static final PersistentSet<Integer> SET;
    
    static {
        SET = new PersistentSet<Integer>().conjAll(integers());
    }
    
    static Set<Integer> integers() {
        Random random = new Random(2004);
        Set<Integer> set = Sets.newLinkedHashSet();
        for (int i=1; i <= 257; i++) {
            set.add(random.nextInt());
        }
        return set;
    }
    
    @Test
    public void Set_Equals() {
        assertThat(SET.asImmutableSet(), equalTo(integers()));
    }
    
    @Test
    public void Removal() {
        MutableSet<Integer> set = SET.toMutableSet();
        for (Integer e : set) {
            set.disjoin(e);
        }
        assertThat(set.size(), equalTo(0));
    }
    
    @Test
    public void Remove_Missing_Value() {
        assertThat(SET.disjoin(123), sameInstance(SET));
    }
    
    @Test
    public void Add_All() {
        PersistentSet<Integer> set = new PersistentSet<Integer>();
        Set<Integer> ints = integers();
        set = set.conjAll(ints);
        assertThat(set.asImmutableSet(), equalTo(ints));
    }
    
    @Test
    public void Add_Incremental() {
        PersistentSet<Integer> set = new PersistentSet<Integer>();
        Set<Integer> ints = integers();
        for (Integer integer : ints) {
            set = set.conj(integer);
        }
        assertThat(set.asImmutableSet(), equalTo(ints));
    }

}
