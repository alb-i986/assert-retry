package me.alb_i986.testing.assertions.retry.internal;

import me.alb_i986.testing.assertions.retry.SubException;
import me.alb_i986.testing.assertions.retry.SuperException;
import org.junit.Test;

import static org.junit.Assert.*;

public class RetryOnExceptionTest {

    @Test
    public void shouldBeOffWhenConfiguredTypeIsNull() {
        RetryOnException sut = new RetryOnException(null);

        assertTrue(sut.isOff());
        assertFalse(sut.matches(new Exception()));
    }

    @Test
    public void shouldMatchSubtypeAndSameType() {
        RetryOnException sut = new RetryOnException(SuperException.class);

        assertTrue(sut.matches(new SubException()));
        assertTrue(sut.matches(new SuperException()));
        assertFalse(sut.isOff());
    }

    @Test
    public void shouldNotMatchSuperType() {
        RetryOnException sut = new RetryOnException(SubException.class);

        assertFalse(sut.matches(new SuperException()));
        assertFalse(sut.isOff());
    }
}