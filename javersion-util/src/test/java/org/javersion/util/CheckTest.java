package org.javersion.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class CheckTest {

    @Test
    public void notNull() {
        String result = Check.notNull("result", "result");
        assertThat(result).isEqualTo("result");
    }

    @Test
    public void notNull_error() {
        try {
            Check.notNull(null, "result");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("result should not be null");
        }
    }

    @Test
    public void notNullOrEmptyString() {
        String result = Check.notNullOrEmpty("result", "field");
        assertThat(result).isEqualTo("result");
    }
    @Test
    public void notNullOrEmptyString_null_error() {
        try {
            Check.notNull(null, "field");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("field should not be null");
        }
    }
    @Test
    public void notNullOrEmptyString_empty_error() {
        try {
            Check.notNull("", "field");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("field should not be empty");
        }
    }

    @Test
    public void notNullOrEmptyIterable() {
        Iterable<String> input = ImmutableList.of("value");
        Iterable<String> result = Check.notNullOrEmpty(input, "field");
        assertThat(result).isEqualTo(input);
    }
    @Test
    public void notNullOrEmptyIterable_null_error() {
        try {
            Iterable<String> input = null;
            Check.notNull(input, "field");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("field should not be null");
        }
    }
    @Test
    public void notNullOrEmptyIterator_empty_error() {
        try {
            Iterable<String> input = ImmutableList.of();
            Check.notNull(input, "field");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("field should not be empty");
        }
    }

    @Test
    public void notNullOrEmptyCollection() {
        List<String> input = ImmutableList.of("value");
        List<String> result = Check.notNullOrEmpty(input, "field");
        assertThat(result).isEqualTo(input);
    }
    @Test
    public void notNullOrEmptyCollection_null_error() {
        try {
            List<String> input = null;
            Check.notNull(input, "field");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("field should not be null");
        }
    }
    @Test
    public void notNullOrEmptyCollection_empty_error() {
        try {
            List<String> input = ImmutableList.of();
            Check.notNull(input, "field");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("field should not be empty");
        }
    }

    @Test
    public void notNullOrEmptyMap() {
        Map<String, String> input = com.google.common.collect.ImmutableMap.of("key", "value");
        Map<String, String> result = Check.notNullOrEmpty(input, "field");
        assertThat(result).isEqualTo(input);
    }
    @Test
    public void notNullOrEmptyMap_null_error() {
        try {
            Map<String, String> input = null;
            Check.notNull(input, "mapField");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("mapField should not be null");
        }
    }
    @Test
    public void notNullOrEmptyMap_empty_error() {
        try {
            Map<String, String> input = com.google.common.collect.ImmutableMap.of();
            Check.notNull(input, "mapField");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("mapField should not be empty");
        }
    }

    @Test
    public void that0() {
        Check.that(true, "should be true");
    }
    @Test
    public void that0_error() {
        try {
            Check.that(false, "should be true");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("should be true");
        }
    }

    @Test
    public void that1() {
        Check.that(true, "%s should be true", "argument");
    }
    @Test
    public void that1_error() {
        try {
            Check.that(false, "%s should be true", "argument");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("argument should be true");
        }
    }

    @Test
    public void that2() {
        Check.that(true, "%s %s be true", "argument", "should");
    }
    @Test
    public void that2_error() {
        try {
            Check.that(false, "%s %s be true", "argument", "should");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("argument should be true");
        }
    }

    @Test
    public void that3() {
        Check.that(true, "%s %s %s true", "argument", "should", "be");
    }
    @Test
    public void that3_error() {
        try {
            Check.that(false, "%s %s %s true", "argument", "should", "be");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("argument should be true");
        }
    }

    @Test
    public void that4() {
        Check.that(true, "%s %s %s %s", "argument", "should", "be", "true");
    }
    @Test
    public void that4_error() {
        try {
            Check.that(false, "%s %s %s %s", "argument", "should", "be", "true");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("argument should be true");
        }
    }

}
