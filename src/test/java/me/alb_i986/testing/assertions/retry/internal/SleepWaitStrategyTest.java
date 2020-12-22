package me.alb_i986.testing.assertions.retry.internal;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.Duration;

import static org.mockito.Mockito.verify;

public class SleepWaitStrategyTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private SystemSleeper sysSleeper;

    @Test
    public void shouldTruncateDurationToLongMaxValue() throws Exception {
        SleepWaitStrategy sut = new SleepWaitStrategy(Duration.ofDays(Long.MAX_VALUE / 100000), sysSleeper);

        sut.runWait();

        verify(sysSleeper).sleep(Long.MAX_VALUE);
    }

    @Test
    public void shouldNotTruncateDuration() throws Exception {
        SleepWaitStrategy sut = new SleepWaitStrategy(Duration.ofDays(1), sysSleeper);

        sut.runWait();

        verify(sysSleeper).sleep(24 * 60 * 60 * 1000);
    }
}