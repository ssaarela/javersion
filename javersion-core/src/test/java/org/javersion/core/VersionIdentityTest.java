package org.javersion.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.core.VersionType.RESET;

import java.util.Arrays;
import java.util.Collection;

import org.javersion.core.SimpleVersion.Builder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@RunWith(Parameterized.class)
public class VersionIdentityTest {

    private final SimpleVersion v1;

    private final Object v2;

    private final boolean shouldEqual;

    public VersionIdentityTest(String desc, SimpleVersion v1, Object v2, boolean shouldEqual) {
        this.v1 = v1;
        this.v2 = v2;
        this.shouldEqual = shouldEqual;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        Builder builder = new Builder(new Revision());
        Version v = builder.build();

        return Arrays.asList(new Object[][] {
                { "same instance", v, v, true },
                { "equal", builder.build(), builder.build(), true },
                { "different revision", v, new Builder().build(), false },

                { "empty parents", builder.build(),
                        builder.parents(ImmutableSet.of()).build(), true },

                { "empty changeset", builder.build(),
                        builder.changeset(ImmutableMap.of()).build(), true },

                { "different type", builder.build(),
                        builder.type(RESET).build(), false },

                { "different branch", builder.build(),
                        builder.branch("branch").build(), false },

                { "different parents", builder.build(),
                        builder.parents(new Revision()).build(), false },

                { "different changeset", builder.build(),
                        builder.changeset(ImmutableMap.of("some", "key")).build(), false },

                { "different meta", builder.build(),
                        builder.meta("meta").build(), false },

                { "other object", builder.build(), new Object(), false }
        });
    }

    @Test
    public void equality() {
        if (shouldEqual) {
            assertThat(v1).isEqualTo(v2);
            assertThat(v2).isEqualTo(v1);
        } else {
            assertThat(v1).isNotEqualTo(v2);
            assertThat(v2).isNotEqualTo(v1);
        }
    }

    @Test
    public void hash() {
        if (shouldEqual) {
            assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
        } else {
            assertThat(v1.hashCode()).isNotEqualTo(v2.hashCode());
        }
    }

    @Test
    public void to_string() {
        if (shouldEqual) {
            assertThat(v1.toString()).isEqualTo(v2.toString());
        } else {
            assertThat(v1.toString()).isNotEqualTo(v2.toString());
        }
    }
}
