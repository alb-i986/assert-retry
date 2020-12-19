package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.Timeout;
import me.alb_i986.testing.assertions.retry.internal.WaitStrategies;

import java.time.Duration;

/**
 * Provides a fluent DSL for configuring the retry mechanism.
 */
public class RetryConfigBuilder {

    private WaitStrategy waitStrategy;
    private Boolean retryOnException;
    private Timeout timeout;

    /**
     * Stop retrying when the timeout expires.
     *
     * @throws IllegalArgumentException if time is not a positive number
     */
    public RetryConfigBuilder timeoutAfter(Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("Duration must not be null");
        }
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        return timeout(new Timeout(duration));
    }

    protected RetryConfigBuilder timeout(Timeout timeout) {
        if (timeout == null) {
            throw new IllegalArgumentException("null timeout");
        }
        this.timeout = timeout;
        return this;
    }

    public RetryConfigBuilder sleepForMillis(long millis) {
        return sleepFor(Duration.ofMillis(millis));
    }

    /**
     * Set {@link WaitStrategies#sleep(Duration)} as the wait strategy.
     *
     * @throws IllegalArgumentException if the duration is not positive, or if it's null
     *
     * @see WaitStrategies#sleep(Duration)
     * @see #waitStrategy(WaitStrategy)
     */
    public RetryConfigBuilder sleepFor(Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("Duration must not be null");
        }
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        return waitStrategy(WaitStrategies.sleep(duration));
    }

    /**
     * Allows to set a custom strategy to wait between attempts,
     * e.g. "wait for the event X to happen".
     * <p>
     * Please note: it is recommended to have the custom strategy override {@code toString()}
     * so that it returns a meaningful description of the strategy,
     * e.g. "waiting for a message to be published on the queue myQueue".
     * This will make the logs more meaningful.
     */
    public RetryConfigBuilder waitStrategy(WaitStrategy waitStrategy) {
        if (waitStrategy == null) {
            throw new IllegalArgumentException("null strategy");
        }
        this.waitStrategy = waitStrategy;
        return this;
    }

    /**
     * Whether we should retry when the supplier of actual values throws an exception.
     */
    public RetryConfigBuilder retryOnException(boolean retryOnException) {
        this.retryOnException = retryOnException;
        return this;
    }

    /**
     * Creates and returns an instance of {@link RetryConfig},
     * configured according to the previous calls to the setter methods.
     *
     * @return a configured instance of {@link RetryConfig}
     *
     * @throws IllegalStateException In case a parameter was not explicitly set.
     */
    public RetryConfig build() {

        //TODO let end users define their own defaults
//        Runnable waitStrategy = this.waitStrategy == null ? DefaultValues.WAIT_STRATEGY : this.waitStrategy;
//        boolean retryOnException = this.retryOnException == null ? DefaultValues.RETRY_ON_EXCEPTION : this.retryOnException;
//        int maxAttempts = this.maxAttempts == null ? DefaultValues.MAX_ATTEMPTS : this.maxAttempts;
//        Timeout timeout = this.timeout == null ? DefaultValues.TIMEOUT : this.timeout;

        if (timeout == null) {
            throw new IllegalStateException("The timeout must be specified");
        }
        if (waitStrategy == null) {
            throw new IllegalStateException("The wait strategy must be specified");
        }
        if (retryOnException == null) {
            throw new IllegalStateException("Should we retry in case the Supplier throws an exception?");
        }
        return new RetryConfig(timeout, waitStrategy, retryOnException);
    }
}
