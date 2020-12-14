package me.alb_i986.testing.assertions.retry.internal;

import java.time.Duration;

public class Timeout {

    private final Duration timeoutDuration;

    private Long startTimeNanos;

    public Timeout(Duration timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
    }

    //TODO allow unit tests to inject a mocked Clock (with Joda time or jdk8)

    /**
     * @throws IllegalStateException if the timeout has already been started and not reset
     */
    public void start() {
        if (startTimeNanos != null) {
            throw new IllegalStateException("Timeout had already been started and not reset");
        }
        this.startTimeNanos = System.nanoTime();
    }

    /**
     * Reset the timeout so that it can be started again.
     */
    public void reset() {
        this.startTimeNanos = null;
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
        return Duration.ofNanos(getElapsedNanos()).compareTo(timeoutDuration) > 0;
    }

    protected long getElapsedNanos() {
        return System.nanoTime() - startTimeNanos;
    }

    public Duration getDuration() {
        return timeoutDuration;
    }
}
