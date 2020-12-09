package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.Timeout;

import java.util.concurrent.TimeUnit;

/**
 * Immutable.
 * Use {@link RetryConfigBuilder} to build an instance.
 *
 * @see RetryConfigBuilder
 */
public class RetryConfig {

    private final boolean retryOnException;
    private final Long maxAttempts;
    private final Runnable waitStrategy;
    private final Timeout timeout;

    RetryConfig(Long maxAttempts, Runnable waitStrategy, boolean retryOnException, Timeout timeout) {
        this.maxAttempts = maxAttempts;
        this.waitStrategy = waitStrategy;
        this.retryOnException = retryOnException;
        this.timeout = timeout;
    }

    public static RetryConfigBuilder builder() {
        return new RetryConfigBuilder();
    }

    /**
     * @see RetryConfigBuilder#retryOnException(boolean)
     */
    public boolean isRetryOnException() {
        return retryOnException;
    }

    /**
     * @see RetryConfigBuilder#maxAttempts(long)
     */
    public Long getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * @see RetryConfigBuilder#waitStrategy(Runnable)
     */
    public Runnable getWaitStrategy() {
        return waitStrategy;
    }

    /**
     * @see RetryConfigBuilder#timeoutAfter(long, TimeUnit)
     */
    public Timeout getTimeout() {
        return timeout;
    }
}
