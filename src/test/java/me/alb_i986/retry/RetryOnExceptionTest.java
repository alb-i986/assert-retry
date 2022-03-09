package me.alb_i986.retry;

import me.alb_i986.retry.testutils.IntermediateException;
import me.alb_i986.retry.testutils.SubException;
import me.alb_i986.retry.testutils.SuperException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RetryOnExceptionTest {

    @Test
    public void whenOff() {
        RetryOnException sut = new RetryOnException(null);

        assertFalse(sut.isOn());
        assertTrue(sut.matches(new Exception()));
    }

    @Test
    public void whenOn() {
        RetryOnException sut = new RetryOnException(IntermediateException.class);

        assertTrue(sut.isOn());
        assertFalse(sut.matches(new SuperException()));
        assertTrue(sut.matches(new IntermediateException()));
        assertTrue(sut.matches(new SubException()));
    }
}
