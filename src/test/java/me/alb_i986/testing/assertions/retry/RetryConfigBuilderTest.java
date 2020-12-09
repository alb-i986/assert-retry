package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.WaitStrategies;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class RetryConfigBuilderTest {

    private final RetryConfigBuilder sut = new RetryConfigBuilder();

    @Test
    public void shouldNotBuildConfigWithoutBothTimeoutAndMaxAttempts() {
        try {
            sut.retryOnException(false)
                    .waitStrategy(WaitStrategies.sleep(5))
                    .build();
            fail("exception expected");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("You must specify either the timeout or the max number attempts"));
        }
    }

    @Test
    public void canBuildConfigWithTimeoutButWithoutMaxAttempts() {
        sut.retryOnException(false)
                .waitStrategy(WaitStrategies.sleep(5))
                .timeoutAfter(2, TimeUnit.SECONDS)
                .build();
    }

    @Test
    public void canBuildConfigWithMaxAttemptsButWithoutTimeout() {
        sut.retryOnException(false)
                .waitStrategy(WaitStrategies.sleep(5))
                .maxAttempts(5)
                .build();
    }
}