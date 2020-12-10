package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.Timeout;
import me.alb_i986.testing.assertions.retry.internal.WaitStrategies;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class RetryConfigBuilderTest {

    private final RetryConfigBuilder sut = new RetryConfigBuilder();

    @Test
    public void cantBuildConfigWithoutTimeout() {
        try {
            sut.retryOnException(false)
                    .waitStrategy(WaitStrategies.sleep(5))
                    .build();
            fail("exception expected");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("timeout"));
        }
    }

    @Test
    public void cantBuildConfigWithoutWaitStrategy() {
        try {
            sut.timeout(new Timeout(1, TimeUnit.SECONDS))
                    .retryOnException(false)
                    .build();
            fail("exception expected");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("wait"));
        }
    }

    @Test
    public void cantBuildConfigWithoutRetryOnException() {
        try {
            sut.timeout(Timeout.INFINITE_TIMEOUT)
                    .waitStrategy(() -> {})
                    .build();
            fail("exception expected");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("exception"));
        }
    }
}