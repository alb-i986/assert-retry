package me.alb_i986.testing.assertions.retry.internal;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

public class TimeoutTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Clock mockClock;

    private final Instant INSTANT = Instant.now();
    private final int timeoutDuration = 100;
    private Timeout aHundredMillisTimeout;

    @Before
    public void setUp() {
        aHundredMillisTimeout = new Timeout(Duration.ofMillis(timeoutDuration), mockClock);
    }

    @Test
    public void timeoutExpired() {
        given(mockClock.instant())
                .willReturn(INSTANT)
                .willReturn(INSTANT.plusMillis(timeoutDuration + 1));

        aHundredMillisTimeout.start();

        assertTrue(aHundredMillisTimeout.isExpired());
    }

    @Test
    public void timeoutNotExpired() {
        given(mockClock.instant())
                .willReturn(INSTANT)
                .willReturn(INSTANT.plusMillis(timeoutDuration - 1));

        aHundredMillisTimeout.start();

        assertFalse(aHundredMillisTimeout.isExpired());
    }

    @Test
    public void timeoutAlreadyStarted() {
        given(mockClock.instant())
                .willReturn(INSTANT);

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
        given(mockClock.instant())
                .willReturn(INSTANT);
        
        // given
        aHundredMillisTimeout.start();

        // when
        aHundredMillisTimeout.reset();
        aHundredMillisTimeout.start();
    }
}