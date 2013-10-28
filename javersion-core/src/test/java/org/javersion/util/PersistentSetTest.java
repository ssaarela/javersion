package org.javersion.util;

import java.util.Random;
import java.util.Set;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

import com.google.common.collect.Sets;

public class PersistentSetTest {

    private static final PersistentSet<Integer> SET;
    
    static {
        SET = new PersistentSet<Integer>().conjAll(integers());
    }
    
    private static Set<Integer> integers() {
        Random random = new Random(2004);
        Set<Integer> set = Sets.newLinkedHashSet();
        for (int i=1; i <= 257; i++) {
            set.add(random.nextInt());
        }
        return set;
    }
    
    @Test
    public void As_Set() {
        Set<Integer> ints = integers();
        Set<Integer> atomicSet = SET.asSet();
        assertThat(atomicSet, equalTo(ints));
        
        for (Integer e : atomicSet) {
            assertThat(atomicSet.remove(e), equalTo(ints.remove(e)));
        }
        assertThat(atomicSet.isEmpty(), equalTo(true));
        
        assertThat(atomicSet.remove(123), equalTo(ints.remove(123)));
        
        ints.addAll(SET.asSet());
        atomicSet.addAll(ints);
        assertThat(atomicSet, equalTo(ints));
        
        atomicSet.clear();
        assertThat(atomicSet.isEmpty(), equalTo(true));

        for (Integer integer : ints) {
            atomicSet.add(integer);
        }
        assertThat(atomicSet, equalTo(ints));
    }
}
