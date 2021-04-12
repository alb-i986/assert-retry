package me.alb_i986.retry;

import me.alb_i986.retry.wait.SleepWaitStrategy;
import me.alb_i986.retry.wait.WaitStrategy;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

public class RetryConfigBuilder<T> {

    private WaitStrategy waitStrategy;
    private Class<? extends Exception> exceptionType;
    private Timeout timeout;

    public RetryConfigBuilder<T> within(long amount, TemporalUnit unit) {
        return within(new Timeout(Duration.of(amount, unit)));
    }

    protected RetryConfigBuilder<T> within(Timeout timeout) {
        this.timeout = timeout;
        return this;
    }

    public RetryConfigBuilder<T> sleepForSeconds(long seconds) {
        try {
            return withWaitStrategy(new SleepWaitStrategy(
                    Duration.ofSeconds(seconds).toMillis()));
        } catch (ArithmeticException e) {
            throw new RetryConfigException("Please set a lower sleep time. Max=" + Long.MAX_VALUE / 1000 + "s", e);
        }
    }

    public RetryConfigBuilder<T> sleepForMillis(long millis) {
        return withWaitStrategy(new SleepWaitStrategy(millis));
    }

    public RetryConfigBuilder<T> withWaitStrategy(WaitStrategy waitStrategy) {
        if (waitStrategy == null) {
            throw new RetryConfigException("The wait strategy cannot be null");
        }
        if (this.waitStrategy != null) {
            throw new RetryConfigException("A wait strategy has already been set");
        }
        this.waitStrategy = waitStrategy;
        return this;
    }

    public RetryConfigBuilder<T> ignoring(Class<? extends Exception> exceptionType) {
        this.exceptionType = exceptionType;
        return this;
    }

    public RetryConfig build() {

        //TODO let end users define their own defaults
//        Runnable waitStrategy = this.waitStrategy == null ? DefaultValues.WAIT_STRATEGY : this.waitStrategy;
//        boolean retryOnException = this.retryOnException == null ? DefaultValues.RETRY_ON_EXCEPTION : this.retryOnException;
//        int maxAttempts = this.maxAttempts == null ? DefaultValues.MAX_ATTEMPTS : this.maxAttempts;
//        Timeout timeout = this.timeout == null ? DefaultValues.TIMEOUT : this.timeout;

        checkMandatoryParams();
        return new RetryConfig(timeout, waitStrategy, new RetryOnException(exceptionType));
    }

    protected void checkMandatoryParams() {
        StringBuilder sb = new StringBuilder();
        if (timeout == null) {
            sb.append(System.lineSeparator())
                    .append("\t- timeout: did you call within()?");
        }
        if (waitStrategy == null) {
            sb.append(System.lineSeparator())
                    .append("\t- wait strategy: " +
                            "please call one of sleepForMillis(), sleepForSeconds(), or withWaitStrategy()");
        }
        if (sb.length() > 0) {
            throw new RetryConfigException("One or more mandatory parameters have not been set:" + sb);
        }
    }
}
