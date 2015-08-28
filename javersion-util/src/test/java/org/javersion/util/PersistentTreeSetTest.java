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

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;
import static java.util.Spliterator.SORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.*;
import java.util.stream.StreamSupport;

import org.junit.Test;

public class PersistentTreeSetTest {

    private static final Random RANDOM = new Random(2007);

    private static Set<Integer> randoms(int count) {
        Set<Integer> set = new LinkedHashSet<>();
        for (int i=0; i < count; i++) {
            set.add(RANDOM.nextInt());
        }
        return set;
    }

    private static final PersistentTreeSet<Integer> NUM_SET;
    private static final int NUM_SET_SUM;

    static {
        PersistentTreeSet<Integer> set = PersistentTreeSet.empty();
        int sum = 0;
        for (int i=1; i <= 1000000; i++) {
            set = set.conj(i);
            sum += i;
        }
        NUM_SET = set;
        NUM_SET_SUM = sum;
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

    @Test
    public void reduce_sum() {
        assertThat(NUM_SET.stream().reduce(Integer::sum).get()).isEqualTo(NUM_SET_SUM);
    }

    @Test
    public void empty_set_spliterator() {
        assertThat(PersistentTreeSet.empty().spliterator().tryAdvance(n -> fail() )).isEqualTo(false);
        assertThat(PersistentTreeSet.empty().spliterator().trySplit()).isNull();
    }

    @Test
    public void try_split_too_small_set() {
        assertThat(PersistentTreeSet.empty().conj(1).conj(2).spliterator().trySplit()).isNull();

        Spliterator<Integer> spliterator = PersistentTreeSet.of(1, 2, 3).spliterator();
        // Initialize stack, ignore result which is covered by other tests
        spliterator.tryAdvance(n -> {});

        assertThat(spliterator.trySplit()).isNull();
    }

    @Test
    public void spliterator_size() {
        Spliterator<Integer> spliterator = PersistentTreeSet.of(1, 2, 3).spliterator();
        assertThat(spliterator.estimateSize()).isEqualTo(3);
        assertThat(spliterator.hasCharacteristics(SIZED)).isEqualTo(true);

        Spliterator<Integer> prefix = spliterator.trySplit();
        assertThat(prefix).isNotNull();

        assertThat(spliterator.estimateSize()).isEqualTo(1);
        assertThat(prefix.estimateSize()).isEqualTo(1);

        assertThat(spliterator.hasCharacteristics(SIZED)).isEqualTo(true);
        assertThat(prefix.hasCharacteristics(SIZED)).isEqualTo(false);
    }

    @Test
    public void spliterator_characteristics() {
        Spliterator<Integer> spliterator = PersistentTreeSet.of(1).spliterator();
        assertThat(spliterator.hasCharacteristics(SIZED | ORDERED | SORTED | DISTINCT | IMMUTABLE)).isEqualTo(true);
    }

    @Test
    public void empty_set_toString() {
        assertThat(PersistentTreeSet.empty().toString()).isEqualTo("[]");
    }

    @Test
    public void set_toString() {
        assertThat(PersistentTreeSet.of(1, 2, 3).toString()).isEqualTo("[1, 2, 3]");
    }

    @Test
    public void reduce_partially_consumed_spliterator() {
        Spliterator<Integer> spliterator = NUM_SET.spliterator();
        assertThat(spliterator.tryAdvance(n -> assertThat(n).isEqualTo(1))).isEqualTo(true);
        assertThat(spliterator.tryAdvance(n -> assertThat(n).isEqualTo(2))).isEqualTo(true);

        assertThat(StreamSupport.stream(spliterator, false).reduce(Integer::sum).get()).isEqualTo(NUM_SET_SUM - 2 - 1);
    }

    @Test
    public void parallel_reduce_sum() {
        assertThat(NUM_SET.parallelStream().reduce(Integer::sum).get()).isEqualTo(NUM_SET_SUM);
    }

    private void assertSets(List<PersistentTreeSet<Integer>> sets, Set<Integer> ints) {
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
