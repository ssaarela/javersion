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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.assertj.core.api.Assertions;
import org.javersion.util.PersistentHashMapTest.HashKey;
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
    public void empty_set_contains() {
        assertThat(new PersistentHashSet<Object>().contains("foo")).isFalse();
    }

    @Test
    public void Set_Equals() {
        assertThat(SET.asSet()).isEqualTo(integers());
    }

    @Test
    public void Removal() {
        MutableHashSet<Integer> set = SET.toMutableSet();
        for (Integer e : set) {
            set.remove(e);
        }
        assertThat(set.size()).isEqualTo(0);
    }

    @Test
    public void Remove_Missing_Value() {
        assertThat(SET.disj(123)).isSameAs(SET);
    }

    @Test
    public void Add_All() {
        PersistentHashSet<Integer> set = new PersistentHashSet<>();
        Set<Integer> ints = integers();
        set = set.conjAll(ints);
        assertThat(set.asSet()).isEqualTo(ints);
    }

    @Test
    public void Add_Incremental() {
        PersistentHashSet<Integer> set = new PersistentHashSet<>();
        Set<Integer> ints = integers();
        for (Integer integer : ints) {
            set = set.conj(integer);
        }
        assertThat(set.asSet()).isEqualTo(ints);
    }

    @Test
    public void reduce() {
        PersistentHashSet<HashKey> set = new PersistentHashSet<>();
        int sum=0;
        int count=0;
        // ArrayNode
        for (int i=0; i < 32; i++) {
            sum+=i;
            count++;
            set = set.conj(new HashKey(i));
        }
        // HashNode
        for (int i=1; i < 5; i++) {
            int num = i<<(4 + i);
            sum+=num;
            count++;
            set = set.conj(new HashKey(num));
        }
        // CollisionNodes
        set = set.conj(new HashKey(1));
        sum+=1;
        count++;
        set = set.conj(new HashKey(1));
        sum+=1;
        count++;

        assertThat(sumOf(set.stream())).isEqualTo(sum);
        assertThat(set.stream().count()).isEqualTo(count);

        assertThat(sumOf(set.parallelStream())).isEqualTo(sum);
        assertThat(set.parallelStream().count()).isEqualTo(count);

        // Reduce partially consumed in parallel
        for (int i=1; i < set.size(); i++) {
            Spliterator<HashKey> spliterator = set.spliterator();
            final MutableInt partialSum = new MutableInt(0);
            for (int j=0; j < i; j++) {
                spliterator.tryAdvance(k -> partialSum.add(k.hash));
            }
            Assertions.assertThat(sumOf(StreamSupport.stream(spliterator, true)) + partialSum.intValue()).isEqualTo(sum);
        }
    }

    @Test
    public void split_till_the_end() {
        PersistentHashSet<Integer> ints = new PersistentHashSet<Integer>().conjAll(integers());
        List<Spliterator<Integer>> spliterators = new ArrayList<>();
        spliterators.add(ints.spliterator());
        int size = 0;
        while(size != spliterators.size()) {
            size = spliterators.size();
            for (int i=size-1; i >= 0; i--) {
                Spliterator<Integer> spliterator = spliterators.get(i);
                Spliterator<Integer> split = spliterator.trySplit();
                if (split != null) {
                    spliterators.add(split);
                }
            }
        }
        final MutableLong sum = new MutableLong(0);
        for (Spliterator<Integer> spliterator : spliterators) {
            while (spliterator.tryAdvance(i -> sum.add(i)));
        }
        Assertions.assertThat(sum.longValue()).isEqualTo(
                ints.stream().map(Long::new).reduce(Long::sum).get());
    }

    @Test
    public void try_split_single_entry() {
        PersistentHashSet<Integer> set = new PersistentHashSet<Integer>().conj(5);
        assertThat(set.spliterator().trySplit()).isNull();

        Spliterator<Integer> spliterator = set.spliterator();
        assertThat(spliterator.tryAdvance(i-> assertThat(i).isEqualTo(5) )).isTrue();
        assertThat(spliterator.trySplit()).isNull();
    }

    @Test
    public void try_split_sub_spliterator() {
        PersistentHashSet<Integer> set = new PersistentHashSet<Integer>().conj(1).conj(33);

        Spliterator<Integer> spliterator = set.spliterator();
        assertThat(spliterator.tryAdvance(i-> assertThat(i).isEqualTo(1) )).isTrue();
        assertThat(spliterator.trySplit()).isNull();
        assertThat(spliterator.tryAdvance(i -> assertThat(i).isEqualTo(33))).isTrue();
        assertThat(spliterator.tryAdvance(i -> {})).isFalse();
    }

    @Test
    public void to_string() {
        PersistentHashSet<Integer> set = new PersistentHashSet<Integer>();
        assertThat(set.toString()).isEqualTo("[]");

        set = set.conj(1);
        assertThat(set.toString()).isEqualTo("[1]");

        set = set.conj(2);
        assertThat(set.toString()).isEqualTo("[1, 2]");
    }

    private int sumOf(Stream<HashKey> stream) {
        return stream.map(Object::hashCode)
                .reduce(Integer::sum).get();
    }

    @Test
    public void empty_stream() {
        assertThat(new PersistentHashSet<>().stream().count()).isEqualTo(0);
    }
}
