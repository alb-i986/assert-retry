package me.alb_i986.retry;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class Timeout {

    private final Clock clock;
    private final Duration duration;

    private Instant start;
    private Instant end;

    public Timeout(Duration duration) {
        this(duration, Clock.systemDefaultZone());
    }

    /**
     * For testing purposes only.
     *
     * @deprecated please use {@link #Timeout(Duration)} instead
     */
    @Deprecated
    public Timeout(Duration duration, Clock clock) {
        if (duration == null) {
            throw new RetryConfigException("Duration is null");
        }
        if (duration.isNegative()) {
            throw new RetryConfigException("Duration is negative");
        }
        this.duration = duration;
        this.clock = clock;
    }

    public void start() {
        start = now();
        end = start.plus(duration);
    }

    /**
     * @return true if the timeout has expired since the last time it was started
     */
    public boolean isExpired() {
        return now().isAfter(end);
    }

    public Duration getDuration() {
        return duration;
    }

    public Duration getElapsed() {
        return Duration.between(start, now());
    }

    private Instant now() {
        return clock.instant();
    }
}
