package me.alb_i986.testing.assertions.retry.internal;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class RetryOnException {

    private final Class<? extends Exception> exceptionType;
    private final Matcher<Class<? extends Exception>> matcher;

    /**
     * @param exceptionType nullable: if null, this is off (see {@link #isOff()})
     */
    public RetryOnException(Class<? extends Exception> exceptionType) {
        this.exceptionType = exceptionType;
        this.matcher = exceptionType != null ? Matchers.instanceOf(exceptionType) : null;
    }

    public boolean matches(Exception e) {
        if (matcher == null) {
            return false;
        }
        return matcher.matches(e);
    }

    public boolean isOff() {
        return exceptionType == null;
    }
}
