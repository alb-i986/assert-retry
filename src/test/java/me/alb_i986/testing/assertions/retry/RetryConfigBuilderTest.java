package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.SleepWaitStrategy;
import me.alb_i986.testing.assertions.retry.internal.Timeout;
import org.junit.Test;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class RetryConfigBuilderTest {

    private final RetryConfigBuilder sut = new RetryConfigBuilder();

    @Test
    public void cantBuildConfigWithoutTimeout() {
        try {
            sut.doNotRetryOnException()
                    .sleepForMillis(5)
                    .build();
            fail("exception expected");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("timeout"));
        }
    }

    @Test
    public void cantBuildConfigWithoutWaitStrategy() {
        try {
            sut.timeout(new Timeout(Duration.ofSeconds(1)))
                    .doNotRetryOnException()
                    .build();
            fail("exception expected");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("wait"));
        }
    }

    @Test
    public void cantBuildConfigWithoutRetryOnException() {
        try {
            sut.timeout(new Timeout(Duration.ofMillis(100)))
                    .sleepForMillis(10)
                    .build();
            fail("exception expected");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("exception"));
        }
    }
}