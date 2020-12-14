package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.Timeout;

/**
 * Immutable object.
 * Use {@link RetryConfigBuilder} to build an instance.
 *
 * @see RetryConfigBuilder
 */
public class RetryConfig {

    private final Timeout timeout;
    private final Runnable waitStrategy;
    private final boolean retryOnException;

    RetryConfig(Timeout timeout, Runnable waitStrategy, boolean retryOnException) {
        this.timeout = timeout;
        this.waitStrategy = waitStrategy;
        this.retryOnException = retryOnException;
    }

    public static RetryConfigBuilder builder() {
        return new RetryConfigBuilder();
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public Runnable getWaitStrategy() {
        return waitStrategy;
    }

    public boolean isRetryOnException() {
        return retryOnException;
    }
}
