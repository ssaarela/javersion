package org.javersion.core;

import org.junit.Test;

public class SimpleVersionTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannot_have_self_as_parent() {
        Revision rev = new Revision();
        new SimpleVersion.Builder(rev)
                .parents(rev)
                .build();
    }
}
