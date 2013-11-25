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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class PersitentTreeSetTest {
    
    private static final Random RANDOM = new Random(2007);
    
    private Set<Integer> randoms(int count) {
        Set<Integer> set = new LinkedHashSet<>();
        for (int i=0; i < count; i++) {
            set.add(RANDOM.nextInt());
        }
        return set;
    }
    
    @Test
    public void Immutability_On_Conj_And_Disj() {
        List<PersistentTreeSet<Integer>> sets = new ArrayList<>();
        Set<Integer> ints = randoms(1234);
        PersistentTreeSet<Integer> set = PersistentTreeSet.empty();
        sets.add(set);
        for (Integer e : ints) {
            set = set.conj(e);
            sets.add(set);
        }
        assertSets(sets, ints);
        int i=1;
        for (Integer e : ints) {
            set = sets.get(i);
            sets.set(i-1, set.disj(e));
            i++;
        }
        assertSets(sets, ints);
    }

    private void assertSets(List<PersistentTreeSet<Integer>> sets,
            Set<Integer> ints) {
        PersistentTreeSet<Integer> set;
        assertThat(sets.get(0).size(), equalTo(0));
        assertThat(sets.get(0).root(), nullValue());
        for (int i=1; i <= ints.size(); i++) {
            set = sets.get(i);
            assertThat(set.size(), equalTo(i));
            int j=0;
            for (Integer e : ints) {
                if (j++ < i) {
                    assertThat(set.contains(e), equalTo(true));
                } else {
                    assertThat(set.contains(e), equalTo(false));
                }
            }
        }
    }
}
