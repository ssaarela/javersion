package org.javersion.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.javersion.util.PersistentSortedMap.Color;
import org.javersion.util.PersistentSortedMap.Node;
import org.junit.Test;

import com.google.common.collect.Sets;

public class PersistentSortedMapTest {
    
    private static final int RANDOM_SEED = 769335732; //new Random().nextInt();
    
    private static final Random RANDOM = new Random(RANDOM_SEED);

    private static final String DESC = "Random(" + RANDOM_SEED + ")";
    
    @Test
    public void Ascending_Inserts() {
        assertInsert(ascending());
    }
    
    @Test
    public void Ascending_Deletes() {
        assertInsert(ascending());
    }

    private List<Integer> ascending() {
        int size = 100;
        List<Integer> ints = new ArrayList<>(size);
        for (int i=0; i < size; i++) {
            ints.add(i);
        }
        return ints;
    }

    @Test
    public void Descending_Inserts() {
        assertInsert(descending());
    }

    private List<Integer> descending() {
        int size = 100;
        List<Integer> ints = new ArrayList<>(size);
        for (int i=size; i > 0; i--) {
            ints.add(i);
        }
        return ints;
    }

    @Test
    public void Descending_Deletes() {
        assertDelete(descending());
    }
    
    @Test
    public void Random_Inserts() {
        try {
            assertInsert(randoms());
        } catch (AssertionError e) {
            throw new AssertionError(DESC + "->" + e.getMessage(), e);
        }
    }
    
    @Test
    public void Random_Deletes() {
        try {
            assertDelete(randoms());
        } catch (AssertionError e) {
            throw new AssertionError(DESC + "->" + e.getMessage(), e);
        }
    }

    private List<Integer> randoms() {
        int size = 300;
        Set<Integer> ints = Sets.newLinkedHashSetWithExpectedSize(size);
        for (int i=0; i < size; i++) {
            ints.add(RANDOM.nextInt());
        }
        return new ArrayList<>(ints);
    }
    
    @Test
    public void CLR_P269() {
        // Example tree
        assertInsert(
                11,
                2,
                14,
                1,
                7,
                15,
                5,
                8,
                4
                );
        // Same nodes in ascending order
        assertInsert(
                1,
                2,
                4,
                5,
                7,
                8,
                11,
                14,
                15
                );
    }
    
    private void assertInsert(Integer... ints) {
        assertInsert(Arrays.asList(ints));
    }
    
    private void assertInsert(List<Integer> ints) {
        PersistentSortedMap<Integer, Integer> map = PersistentSortedMap.empty();
        List<PersistentSortedMap<Integer, Integer>> maps = new ArrayList<>(ints.size());
        for (Integer i : ints) {
            map = assoc(map, i);
            maps.add(map);
        }

        assertRBMap(maps, ints);
    }

    private void assertDelete(List<Integer> ints) {
        PersistentSortedMap<Integer, Integer> map = PersistentSortedMap.empty();
        List<PersistentSortedMap<Integer, Integer>> maps = new ArrayList<>(ints.size());
        for (Integer i : ints) {
            map = assoc(map, i);
            maps.add(map);
        }
        for (int i = ints.size() - 1; i > 0; i--) {
            map = maps.get(i);
            maps.set(i-1, map.dissoc(i));
        }
        assertRBMap(maps, ints);
    }
    
    private void assertRBMap(
            List<PersistentSortedMap<Integer, Integer>> maps,
            List<Integer> ints) {
        for (int i=0; i < ints.size(); i++) {
            PersistentSortedMap<Integer, Integer> map = maps.get(i);
            assertRBProperties(map.root(), 0);
            assertThat(map.size(), equalTo(i+1));
            for (int j=0; j < ints.size(); j++) {
                Integer key = ints.get(j);
                // Contains all values of previous maps
                if (j <= i) {
                    assertThat(map.get(key), equalTo(key));
                } 
                // But none of the later values
                else {
                    assertThat(map.get(key), nullValue());
                }
            }
        }
    }

    private Integer blacksOnPath = null;
    
    private void assertRBProperties(Node<Integer, Integer> node, int blacks) {
        assertThat(node.color, not(nullValue()));
        if (node.color == Color.RED) {
            assertBlack(node.left);
            assertBlack(node.right);
        } else {
            blacks++;
        }
        boolean leaf = true;
        if (node.left != null){
            assertRBProperties(node.left, blacks);
            leaf = false;
        }
        if (node.right != null) {
            assertRBProperties(node.right, blacks);
            leaf = false;
        }
        if (leaf) {
            if (blacksOnPath == null) {
                blacksOnPath = blacks;
            } else {
                assertThat(blacks, equalTo(blacksOnPath.intValue()));
            }
        }
    }
    
    private void assertBlack(Node<?, ?> node) {
        assertTrue(node == null || node.color == Color.BLACK);
    }
    
    private PersistentSortedMap<Integer, Integer> assoc(PersistentSortedMap<Integer, Integer> map, Integer i) {
        return map.assoc(i, i);
    }
}
