package me.alb_i986.retry;

import java.time.Duration;
import java.util.List;

public class AssertRetryTimeoutError extends AssertRetryError {

    private final Duration timeout;

    protected AssertRetryTimeoutError(Duration timeout, List<RetryResult> results) {
        super("The timeout expired and none of the actual values matched", results);
        this.timeout = timeout;
    }

    public Duration getTimeout() {
        return timeout;
    }
}

