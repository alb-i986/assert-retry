package me.alb_i986.testing.assertions.retry.internal;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

public class TimeoutTest {

    private final Timeout aHundredMillisTimeout = new Timeout(Duration.ofMillis(100));

    @Test
    public void timeoutExpired() throws InterruptedException {
        aHundredMillisTimeout.start();

        Thread.sleep(105);

        assertTrue(aHundredMillisTimeout.isExpired());
    }

    @Test
    public void timeoutNotExpired() throws InterruptedException {
        aHundredMillisTimeout.start();

        Thread.sleep(20);

        assertFalse(aHundredMillisTimeout.isExpired());
    }

    @Test
    public void timeoutAlreadyStarted() {
        aHundredMillisTimeout.start();

        try {
            aHundredMillisTimeout.start();
            fail("did not throw as expected");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void canStartAgainAfterReset() {
        // given
        aHundredMillisTimeout.start();

        // when
        aHundredMillisTimeout.reset();
        aHundredMillisTimeout.start();
    }
}