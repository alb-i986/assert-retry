package me.alb_i986.retry;

import java.util.List;

/**
 * Thrown by {@link AssertRetry} to indicate that the assertion eventually failed.
 */
public class AssertRetryError extends AssertionError {

    private final String reason;
    private final List<RetryResult> results;

    protected AssertRetryError(String reason, List<RetryResult> results) {
        super(forgeMessage(reason, results));
        this.reason = reason;
        this.results = results;
    }

    protected AssertRetryError(String reason, Exception cause, List<RetryResult> results) {
        super(forgeMessage(reason, results), cause);
        this.reason = reason;
        this.results = results;
    }

    private static String forgeMessage(String reason, List<RetryResult> retryResults) {
        return String.format("Retry assertion failed after %d %s: %s",
                retryResults.size(),
                pluralizeWord("attempt", retryResults.size()),
                reason);
    }

    private static String pluralizeWord(String word, int size) {
        return word + (size > 1 ? "s" : "");
    }

    public List<RetryResult> getResults() {
        return results;
    }

    public String getReason() {
        return reason;
    }
}
