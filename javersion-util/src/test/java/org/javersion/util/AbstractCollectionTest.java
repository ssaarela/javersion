package org.javersion.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

public abstract class AbstractCollectionTest {
    
    protected static final int RANDOM_SEED = new Random().nextInt();

    protected static final String DESC = "Random(" + RANDOM_SEED + ")";

    protected List<Integer> ascending(int size) {
        List<Integer> ints = new ArrayList<>(size);
        for (int i=0; i < size; i++) {
            ints.add(i);
        }
        return ints;
    }

    protected List<Integer> descending(int size) {
        List<Integer> ints = new ArrayList<>(size);
        for (int i=size; i > 0; i--) {
            ints.add(i);
        }
        return ints;
    }

    protected List<Integer> randoms(int size) {
        Random random = new Random(RANDOM_SEED);
        Set<Integer> ints = Sets.newLinkedHashSetWithExpectedSize(size);
        for (int i=0; i < size; i++) {
            ints.add(random.nextInt());
        }
        return new ArrayList<>(ints);
    }

}
