package me.alb_i986.testing.assertions.retry.internal;

import me.alb_i986.testing.assertions.retry.RetryConfigBuilder;

import java.time.Duration;

/**
 * Factory methods of wait strategies to be fed into {@link RetryConfigBuilder#waitStrategy(Runnable)}.
 */
public class WaitStrategies {

    protected WaitStrategies() {
        // static class
    }

    /**
     * Sleep for the given amount of time.
     * <p>
     * If the duration is greater than {@link Long#MAX_VALUE}, it will be truncated.
     *
     * @see #sleep(long)
     */
    public static Runnable sleep(Duration duration) {
        try {
            return sleep(duration.toMillis());
        } catch (ArithmeticException e) {
            return sleep(Long.MAX_VALUE);
        }
    }

    /**
     * Sleep for the given amount of time.
     * <p>
     * If the thread is interrupted, it will sleep for a shorter amount of time.
     *
     * @see Thread#sleep(long)
     */
    public static Runnable sleep(long millis) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    // swallow!
                }
            }

            @Override
            public String toString() {
                return "sleep for " +
                        TimeFormatter.SINGLETON.prettyPrint(Duration.ofMillis(millis));
            }
        };
    }
}
