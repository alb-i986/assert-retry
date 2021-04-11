package me.alb_i986.retry.hamcrest;

import me.alb_i986.retry.AssertRetryError;
import me.alb_i986.retry.RetryConfigBuilder;
import me.alb_i986.retry.RetryConfigException;
import me.alb_i986.retry.wait.WaitStrategy;
import org.hamcrest.Matcher;

import java.time.temporal.TemporalUnit;

/**
 * Provides a fluent API for configuring the retry mechanism.
 *
 * <p>The available configurations are:*
 * <dl>
 *     <dt>The {@link #within(long, TemporalUnit) timeout} (mandatory),</dt>
 *     <dd>i.e. how long to retry the assertion for.</dd>
 *
 *     <dt>The wait strategy (mandatory),</dt>
 *     <dd>
 *         i.e. what to do in between failed attempts.
 *         Can be: {@link #sleepForMillis(long)}, {@link #sleepForSeconds(long)},
 *         or {@link #withWaitStrategy(WaitStrategy) a custom strategy}
 *     </dd>
 *
 *     <dt>The {@link #retryOnException(Class) type of exception} the Supplier is expected to throw (optional),</dt>
 *     <dd>i.e. what type of exceptions the {@link RetryMatcher} needs to catch, and retry the assertion upon</dd>
 * </dl>
 * </p>
 *
 * @since 1.0
 */
public class RetryMatcherBuilder<T> {

    private final Matcher<? super T> matcher;

    private final RetryConfigBuilder delegate = new RetryConfigBuilder();

    protected RetryMatcherBuilder(Matcher<? super T> matcher) {
        this.matcher = matcher;
    }

    /**
     * Keep trying running the assertion against a freshly supplied value
     * until the given timeout expires.
     * If the actual value still won't match, an
     * {@link AssertRetryError} will be thrown.
     *
     * @throws RetryConfigException if amount is negative
     * @since 1.0
     */
    public RetryMatcherBuilder<T> within(long amount, TemporalUnit unit) {
        delegate.within(amount, unit);
        return this;
    }

    /**
     * Sleep for the given number of seconds before retrying the assertion,
     * after the previous attempt failed.
     *
     * @throws RetryConfigException if the number of seconds is too big such that it cannot be converted to milliseconds
     * @see #sleepForMillis(long)
     * @since 1.0
     */
    public RetryMatcherBuilder<T> sleepForSeconds(long seconds) {
        delegate.sleepForSeconds(seconds);
        return this;
    }

    /**
     * Sleep for the given number of milliseconds before running the assertion again,
     * after the previous attempt failed.
     *
     * @throws RetryConfigException if the number of milliseconds is negative
     * @see Thread#sleep(long)
     * @since 1.0
     */
    public RetryMatcherBuilder<T> sleepForMillis(long millis) {
        delegate.sleepForMillis(millis);
        return this;
    }

    /**
     * Allows to define a custom wait strategy making it possible to wait for a specific event,
     * rather than randomly sleeping for a pre-defined amount of time.
     *
     * @since 1.0
     */
    public RetryMatcherBuilder<T> withWaitStrategy(WaitStrategy waitStrategy) {
        delegate.withWaitStrategy(waitStrategy);
        return this;
    }

    /**
     * Retry in case the Supplier throws the given type of exception, or lower (a subtype).
     *
     * @param exceptionType the type of exception which we expect the Supplier to throw
     * @since 1.0
     */
    public RetryMatcherBuilder<T> retryOnException(Class<? extends Exception> exceptionType) {
        delegate.retryOnException(exceptionType);
        return this;
    }

    /**
     * @return a configured instance of {@link RetryMatcher}
     * @throws RetryConfigException in case one or more mandatory parameters have not been set.
     * @since 1.0
     */
    public RetryMatcher<T> build() {
        return new RetryMatcher<>(matcher, delegate.build());
    }
}
