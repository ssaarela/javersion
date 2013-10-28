package org.javersion.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void Set_Equals() {
        assertThat(SET.asSet(), equalTo(integers()));
    }
    
    @Test
    public void Removal() {
        Set<Integer> atomicSet = SET.asSet();
        Set<Integer> ints = integers();
        for (Integer e : atomicSet) {
            assertThat(atomicSet.remove(e), equalTo(ints.remove(e)));
        }
        assertThat(atomicSet.isEmpty(), equalTo(true));
    }
    
    @Test
    public void Remove_Missing_Value() {
        assertThat(SET.asSet().remove(123), equalTo(false));
    }
    
    @Test
    public void Add_All() {
        Set<Integer> atomicSet = new AtomicSet<>();
        Set<Integer> ints = integers();
        atomicSet.addAll(ints);
        assertThat(atomicSet, equalTo(ints));
    }
    
    @Test
    public void Add_Incremental() {
        Set<Integer> atomicSet = new AtomicSet<>();
        Set<Integer> ints = integers();
        for (Integer integer : ints) {
            atomicSet.add(integer);
        }
        assertThat(atomicSet, equalTo(ints));
    }
    
    @Test
    public void Clear() {
        Set<Integer> atomicSet = SET.asSet();
        atomicSet.clear();
        assertThat(atomicSet.isEmpty(), equalTo(true));
    }
    
    @Test
    public void Concurrency() throws InterruptedException {
        final int threads = 4;
        final int batchSize = 256;
        final AtomicSet<Integer> atomicSet = new AtomicSet<>();
        final AtomicInteger readCount = new AtomicInteger();
        final CountDownLatch readerCountdown = new CountDownLatch(1);
        final AtomicInteger max = new AtomicInteger();
        Thread reader = new Thread() {
            
            public void run() {
                while (!isInterrupted()) {
                    try {
                        sleep(threads + 1);
                    } catch (InterruptedException e) {}
                    
                    for (Integer i : atomicSet) {
                        max.set(i);
                    }
                    readCount.incrementAndGet();
                    readerCountdown.countDown();
                }
            }
            
        };
        reader.start();

        final CountDownLatch writerCountdown = new CountDownLatch(threads);
        for (int i=0; i < threads; i++) {
            final int thread = i;
            final int start = i* batchSize;
            new Thread() {
                
                public void run() {
                    for (int j=start; j < start + batchSize; j++) {
                        try {
                            sleep(thread);
                        } catch (InterruptedException e) {}
                        atomicSet.add(j);
                    }
                    writerCountdown.countDown();
                }
                
            }.start();
        }
        
        writerCountdown.await();
        reader.interrupt();
        readerCountdown.await();
        
        int reads = readCount.get();
        assertThat(reads, greaterThan(50));
        assertThat(atomicSet.size(), equalTo(threads * batchSize));
    }
}
