package org.javersion.core;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import static org.javersion.core.Version.DEFAULT_BRANCH;

public class BranchAndRevisionTest {

    @Test(expected = IllegalArgumentException.class)
    public void null_versionNode_is_not_allowed() {
        new BranchAndRevision(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_branch_is_not_allowed() {
        new BranchAndRevision(null, new Revision());
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_revision_is_not_allowed() {
        new BranchAndRevision("branch", null);
    }

    @Test
    public void equals_self() {
        BranchAndRevision bar = new BranchAndRevision(DEFAULT_BRANCH, new Revision());
        assertThat(bar).isEqualTo(bar);
    }

    @Test
    public void is_equal() {
        Revision rev = new Revision();
        BranchAndRevision bar1 = new BranchAndRevision(DEFAULT_BRANCH, rev);
        BranchAndRevision bar2 = new BranchAndRevision(DEFAULT_BRANCH, rev);
        assertThat(bar1).isEqualTo(bar2);
        assertThat(bar1.hashCode()).isEqualTo(bar2.hashCode());
    }

    @Test
    public void not_equal_branch() {
        BranchAndRevision bar1 = new BranchAndRevision(DEFAULT_BRANCH, new Revision());
        BranchAndRevision bar2 = new BranchAndRevision("branch", bar1.revision);
        assertThat(bar1).isNotEqualTo(bar2);
        assertThat(bar1.hashCode()).isNotEqualTo(bar2.hashCode());
    }

    @Test
    public void not_equal_revision() {
        BranchAndRevision bar1 = new BranchAndRevision(DEFAULT_BRANCH, new Revision());
        BranchAndRevision rev2 = new BranchAndRevision(DEFAULT_BRANCH, new Revision());
        assertThat(bar1).isNotEqualTo(rev2);
        assertThat(bar1.hashCode()).isNotEqualTo(rev2.hashCode());
    }

    @Test
    public void not_equal_to_other_type() {
        assertThat(new BranchAndRevision(DEFAULT_BRANCH, new Revision()))
                .isNotEqualTo(new Object());
    }
}
