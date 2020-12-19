package me.alb_i986.testing.assertions.retry.internal;

import me.alb_i986.testing.assertions.retry.RetryConfigBuilder;
import me.alb_i986.testing.assertions.retry.WaitStrategy;
import org.hamcrest.Description;

import java.time.Duration;

/**
 * Factory methods of wait strategies to be fed into {@link RetryConfigBuilder#waitStrategy(WaitStrategy)}.
 */
public class WaitStrategies {

    protected WaitStrategies() {
        // static class
    }

    /**
     * Sleeps for the given amount of time.
     * <p>
     * If the duration is greater than {@link Long#MAX_VALUE}, it will be truncated.
     *
     * @see #sleep(long)
     */
    public static WaitStrategy sleep(Duration duration) {
        try {
            return sleep(duration.toMillis());
        } catch (ArithmeticException e) {
            return sleep(Long.MAX_VALUE);
        }
    }

    /**
     * Sleeps for the given amount of time.
     *
     * @see Thread#sleep(long)
     */
    public static WaitStrategy sleep(long millis) {
        return new WaitStrategy() {
            @Override
            public void waitt() throws InterruptedException {
                Thread.sleep(millis);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(toString());
            }

            @Override
            public String toString() {
                return "sleep for " +
                        TimeFormatter.SINGLETON.prettyPrint(Duration.ofMillis(millis));
            }
        };
    }
}
