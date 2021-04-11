package me.alb_i986.retry.wait;

import me.alb_i986.retry.RetryConfigException;
import me.alb_i986.retry.TimeFormatter;

import java.time.Duration;

/**
 * Sleep for the given number of milliseconds.
 *
 * @see Thread#sleep(long)
 * @since 1.0
 */
public class SleepWaitStrategy extends BaseWaitStrategy {

    private final long millis;
    private final Sleeper sleeper;

    public SleepWaitStrategy(long millis) {
        this(millis, new SystemSleeper());
    }

    protected SleepWaitStrategy(long millis, Sleeper sleeper) {
        if (millis < 0) {
            throw new RetryConfigException("The number of millis is negative");
        }
        this.millis = millis;
        this.sleeper = sleeper;
    }

    @Override
    public void runWait() {
        sleeper.sleep(millis);
    }

    @Override
    public String getDescription() {
        return "sleep for "
                + TimeFormatter.SINGLETON.prettyPrint(Duration.ofMillis(millis));
    }
}
