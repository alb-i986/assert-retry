package me.alb_i986.testing.assertions.retry.internal;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class Timeout {

    /**
     * A timeout which never expires.
     */
    public static final Timeout INFINITE_TIMEOUT = new Timeout(Long.MAX_VALUE, TimeUnit.DAYS) {
        @Override
        public boolean isExpired() {
            return false;
        }
    };

    private final long timeout;
    private final TimeUnit timeoutUnit;

    private Long startTimeNanos;

    public Timeout(long timeout, TimeUnit timeoutUnit) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
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
        return getElapsedTimeMillis() > timeoutUnit.toMillis(timeout);
    }

    public long getElapsedTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNanos);
    }

    public Duration getDuration() {
        return Duration.of(timeoutUnit.toNanos(timeout), ChronoUnit.NANOS);
    }
}
