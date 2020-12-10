package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.Timeout;
import me.alb_i986.testing.assertions.retry.internal.WaitStrategies;

import java.util.concurrent.TimeUnit;

/**
 * Provides a fluent DSL for configuring the retry mechanism.
 */
public class RetryConfigBuilder {

    private Runnable waitStrategy;
    private Boolean retryOnException;
    private Long maxAttempts;
    private Timeout timeout;

    /**
     * Set {@link WaitStrategies#sleep(long, TimeUnit)} as the wait strategy.
     *
     * @throws IllegalArgumentException if time is not positive, or if timeUnit is null
     *
     * @see WaitStrategies#sleep(long, TimeUnit)
     * @see #waitStrategy(Runnable)
     */
    public RetryConfigBuilder sleepBetweenAttempts(long time, TimeUnit timeUnit) {
        if (time <= 0) {
            throw new IllegalArgumentException("time must be positive");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("timeUnit must not be null");
        }
        return waitStrategy(WaitStrategies.sleep(time, timeUnit));
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
    public RetryConfigBuilder waitStrategy(Runnable waitStrategy) { // TODO WaitStrategy instead of Runnable, so that we can enforce a getDescription() method
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
     * How many times to run the assertion for, in case it fails.
     *
     * @throws IllegalArgumentException if n &lt; 1
     */
    public RetryConfigBuilder maxAttempts(long n) {
        if (n < 1) {
            throw new IllegalArgumentException("maxAttempts < 1");
        }
        this.maxAttempts = n;
        return this;
    }

    /**
     * Stop retrying when the timeout expires.
     *
     * @throws IllegalArgumentException if time is not a positive number
     */
    public RetryConfigBuilder timeoutAfter(long time, TimeUnit timeUnit) {
        if (time <= 0) {
            throw new IllegalArgumentException("time must be positive");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("timeUnit is null");
        }
        return timeout(new Timeout(time, timeUnit));
    }

    protected RetryConfigBuilder timeout(Timeout timeout) {
        if (timeout == null) {
            throw new IllegalArgumentException("null timeout");
        }
        this.timeout = timeout;
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

        if (maxAttempts == null && timeout == null) {
            throw new IllegalStateException("You must specify either the timeout or the max number attempts");
        } else if (timeout == null) {
            timeout = Timeout.INFINITE_TIMEOUT;
        } else { // maxAttempts == null
            maxAttempts = null;
        }
        if (retryOnException == null) {
            throw new IllegalStateException("Should we retry in case the Supplier throws?");
        }
        if (waitStrategy == null) {
            throw new IllegalStateException("The wait strategy must be specified");
        }

        return new RetryConfig(maxAttempts, waitStrategy, retryOnException, timeout);
    }
}
