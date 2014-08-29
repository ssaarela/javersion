package org.javersion.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class ReflectionEquals<T> extends BaseMatcher<T> {

    public static <T> ReflectionEquals<T> reflectionEquals(T wanted) {
        return new ReflectionEquals<T>(wanted);
    }

    private final T wanted;

    public ReflectionEquals(T wanted) {
        this.wanted = wanted;
    }

    @Override
    public boolean matches(Object item) {
        return EqualsBuilder.reflectionEquals(item, wanted);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(ReflectionToStringBuilder.toString(wanted));
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendText("was ").appendValue(ReflectionToStringBuilder.toString(item));
    }

}
