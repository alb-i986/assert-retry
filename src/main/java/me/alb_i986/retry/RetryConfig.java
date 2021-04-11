package me.alb_i986.retry;

import me.alb_i986.retry.wait.WaitStrategy;

import static java.util.Objects.requireNonNull;

/**
 * Immutable object.
 */
public class RetryConfig {

    private final Timeout timeout;
    private final WaitStrategy waitStrategy;
    private final RetryOnException retryOnException;

    public RetryConfig(Timeout timeout, WaitStrategy waitStrategy, RetryOnException retryOnException) {
        this.timeout = requireNonNull(timeout);
        this.waitStrategy = requireNonNull(waitStrategy);
        this.retryOnException = requireNonNull(retryOnException);
    }

    public Timeout timeout() {
        return timeout;
    }

    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    public RetryOnException getRetryOnException() {
        return retryOnException;
    }
}
