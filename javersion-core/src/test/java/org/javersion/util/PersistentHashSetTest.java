/*
 * Copyright 2013 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javersion.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Random;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

public class PersistentHashSetTest {

    public static final PersistentHashSet<Integer> SET;
    
    static {
        SET = new PersistentHashSet<Integer>().conjAll(integers());
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
        assertThat(SET.asSet(), equalTo(integers()));
    }
    
    @Test
    public void Removal() {
        MutableHashSet<Integer> set = SET.toMutableSet();
        for (Integer e : set) {
            set.remove(e);
        }
        assertThat(set.size(), equalTo(0));
    }
    
    @Test
    public void Remove_Missing_Value() {
        assertThat(SET.disjoin(123), sameInstance(SET));
    }
    
    @Test
    public void Add_All() {
        PersistentHashSet<Integer> set = new PersistentHashSet<Integer>();
        Set<Integer> ints = integers();
        set = set.conjAll(ints);
        assertThat(set.asSet(), equalTo(ints));
    }
    
    @Test
    public void Add_Incremental() {
        PersistentHashSet<Integer> set = new PersistentHashSet<Integer>();
        Set<Integer> ints = integers();
        for (Integer integer : ints) {
            set = set.conj(integer);
        }
        assertThat(set.asSet(), equalTo(ints));
    }

}
