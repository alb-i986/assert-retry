package me.alb_i986.testing.assertions.retry.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class Timeout {

    private final Clock clock;
    private final Duration timeoutDuration;

    private Instant startInstant;
    private Instant endInstant;

    public Timeout(Duration timeoutDuration) {
        this(timeoutDuration, Clock.systemDefaultZone());
    }

    public Timeout(Duration timeoutDuration, Clock clock) {
        this.timeoutDuration = timeoutDuration;
        this.clock = clock;
    }

    /**
     * @throws IllegalStateException if the timeout has already been started and not reset
     */
    public void start() {
        if (startInstant != null) {
            throw new IllegalStateException("Timeout had already been started and not reset");
        }
        this.startInstant = clock.instant();
        this.endInstant = startInstant.plus(timeoutDuration);
    }

    /**
     * Resets the timeout so that it can be started again.
     */
    public void reset() {
        this.startInstant = null;
        this.endInstant = null;
    }

    /**
     * First reset, and then start the timeout.
     */
    public void restart() {
        reset();
        start();
    }

    /**
     * @return true if the timeout has expired since it was started
     */
    public boolean isExpired() {
        Instant now = clock.instant();
        return endInstant.compareTo(now) < 0;
    }

    public Duration getDuration() {
        return timeoutDuration;
    }
}
