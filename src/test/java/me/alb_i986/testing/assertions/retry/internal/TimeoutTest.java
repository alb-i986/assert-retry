package me.alb_i986.testing.assertions.retry.internal;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TimeoutTest {

    private final Timeout oneSecondTimeout = new Timeout(1, TimeUnit.SECONDS);

    @Test
    public void timeoutExpired() throws Exception {
        oneSecondTimeout.start();

        Thread.sleep(1000);

        assertTrue(oneSecondTimeout.isExpired());
    }

    @Test
    public void timeoutNotExpired() throws Exception {
        oneSecondTimeout.start();

        Thread.sleep(20);

        assertFalse(oneSecondTimeout.isExpired());
    }

    @Test
    public void timeoutAlreadyStarted() {
        oneSecondTimeout.start();

        try {
            oneSecondTimeout.start();
            fail("did not throw as expected");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void canStartAgainAfterReset() {
        oneSecondTimeout.start();

        oneSecondTimeout.reset();
        oneSecondTimeout.start();
    }
}