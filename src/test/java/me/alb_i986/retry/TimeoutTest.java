package me.alb_i986.retry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

public class TimeoutTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Clock clockMock;

    private final int timeoutMillis = 5;
    private final Instant startInstant = Instant.now();

    private Timeout aFiveMillisTimeout;

    @Before
    public void setUp() {
        aFiveMillisTimeout = new Timeout(Duration.ofMillis(timeoutMillis), clockMock);
    }

    @Test
    public void timeoutExpired() {
        given(clockMock.instant())
                .willReturn(startInstant)
                .willReturn(startInstant.plusMillis(timeoutMillis + 1));

        aFiveMillisTimeout.start();

        assertTrue(aFiveMillisTimeout.isExpired());
    }

    @Test
    public void timeoutNotExpired() {
        given(clockMock.instant())
                .willReturn(startInstant)
                .willReturn(startInstant.plusMillis(timeoutMillis - 1));

        aFiveMillisTimeout.start();

        assertFalse(aFiveMillisTimeout.isExpired());
        assertThat(aFiveMillisTimeout.getElapsed().toMillis(), equalTo(4L));
    }

    @Test
    public void canStartAgain() {
        given(clockMock.instant())
                .willReturn(startInstant)
                .willReturn(startInstant.plusMillis(5));

        aFiveMillisTimeout.start();
        aFiveMillisTimeout.start();

        assertThat(aFiveMillisTimeout.getElapsed().toMillis(), equalTo(0L));
    }

    @Test
    public void shouldNotCreateInstanceOnNullDuration() {
        assertThrows(IllegalArgumentException.class, () ->
                new Timeout(null));
    }

    @Test
    public void shouldNotCreateInstanceOnNegativeDuration() {
        assertThrows(IllegalArgumentException.class, () ->
                new Timeout(Duration.of(-5, ChronoUnit.SECONDS)));
    }
}