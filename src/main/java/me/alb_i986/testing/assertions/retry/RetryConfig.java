package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.RetryOnException;
import me.alb_i986.testing.assertions.retry.internal.Timeout;

/**
 * Immutable object.
 * Use {@link RetryConfigBuilder} to build an instance.
 *
 * @see RetryConfigBuilder
 */
public class RetryConfig {

    private final Timeout timeout;
    private final WaitStrategy waitStrategy;
    private final RetryOnException retryOnException;

    RetryConfig(Timeout timeout, WaitStrategy waitStrategy, RetryOnException retryOnException) {
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

    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    public RetryOnException getRetryOnException() {
        return retryOnException;
    }
}
