package me.alb_i986.testing.assertions.retry.internal;

import me.alb_i986.testing.assertions.retry.BaseWaitStrategy;

import java.time.Duration;

/**
 * Sleep for the given amount of time.
 * <p>
 * If the duration is greater than {@link Long#MAX_VALUE}, it will be truncated.
 *
 * @see Thread#sleep(long)
 */
public class SleepWaitStrategy extends BaseWaitStrategy {

    private final long millis;
    private final SystemSleeper systemSleeper;

    public SleepWaitStrategy(Duration duration, SystemSleeper systemSleeper) {
        this(durationToMillisOrTruncate(duration), systemSleeper);
    }

    private static long durationToMillisOrTruncate(Duration duration) {
        try {
            return duration.toMillis();
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    public SleepWaitStrategy(long millis, SystemSleeper systemSleeper) {
        this.millis = millis;
        this.systemSleeper = systemSleeper;
    }

    @Override
    public void runWait() throws InterruptedException {
        systemSleeper.sleep(millis);
    }

    @Override
    public String getDescription() {
        return "sleep for " +
                TimeFormatter.SINGLETON.prettyPrint(Duration.ofMillis(millis));
    }
}
