package me.alb_i986.testing.assertions.retry;

import org.junit.Test;

import java.time.Duration;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RetryMatcherIT {

    private static final Duration FIFTY_MILLIS = Duration.ofMillis(50);

    @Test
    public void supplierEventuallyReturnsMatchingValue() {
        long startTimeMillis = System.currentTimeMillis();

        assertThat(Suppliers.fromList("a", "b", "c"),
                RetryMatcher.eventually(containsString("c"),
                        RetryConfig.builder()
                                .timeoutAfter(FIFTY_MILLIS)
                                .sleepBetweenAttempts(Duration.ofMillis(10))
                                .retryOnException(false)
                ));

        long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        assertThat(elapsedTimeMillis, allOf(greaterThan(20L), lessThanOrEqualTo(30L)));
    }

    @Test
    public void supplierDoesNotMatchWithinTimeout() {
        try {
            assertThat(Suppliers.ascendingIntegersStartingFrom(1),
                    RetryMatcher.eventually(greaterThan(6),
                            RetryConfig.builder()
                                    .timeoutAfter(FIFTY_MILLIS)
                                    .sleepBetweenAttempts(Duration.ofMillis(10))
                                    .retryOnException(false)
                    ));
            fail("expected to fail");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("\nExpected: supplied values to *eventually* match a value greater than <6> within 50ms\n" +
                    "     but: The timeout was reached and none of the actual values matched\n" +
                    "          Actual values (in order of appearance):\n" +
                    "           - <1>\n" +
                    "           - <2>\n" +
                    "           - <3>\n" +
                    "           - <4>\n" +
                    "           - <5>\n" +
                    "           - <6>"));
        }
    }

    @Test
    public void supplierThrowsButEventuallyMatches() {
        assertThat(Suppliers.throwing(3, "found"),
                RetryMatcher.eventually(is("found"),
                        RetryConfig.builder()
                                .timeoutAfter(FIFTY_MILLIS)
                                .sleepBetweenAttempts(Duration.ofMillis(10))
                                .retryOnException(true)
                ));
    }

    @Test
    public void supplierThrowsAndEventuallyDoesNotMatch() {
        try {
            assertThat(Suppliers.throwing(3, "never matching actual"),
                    RetryMatcher.eventually(is("expected value"),
                            RetryConfig.builder()
                                    .timeoutAfter(FIFTY_MILLIS)
                                    .sleepBetweenAttempts(Duration.ofMillis(10))
                                    .retryOnException(true)
                    ));
            fail("exception expected");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("\nExpected: supplied values to *eventually* match is \"expected value\" within 50ms\n" +
                    "     but: The timeout was reached and none of the actual values matched\n" +
                    "          Actual values (in order of appearance):\n" +
                    "           - thrown java.lang.RuntimeException: Supplier failed\n" +
                    "           - thrown java.lang.RuntimeException: Supplier failed\n" +
                    "           - thrown java.lang.RuntimeException: Supplier failed\n" +
                    "           - \"never matching actual\"\n" +
                    "           - \"never matching actual\"\n" +
                    "           - \"never matching actual\""));
        }
    }

    @Test
    public void supplierThrowsAndRetryOnExceptionIsOff() {
        try {
            assertThat(Suppliers.throwing(),
                    RetryMatcher.eventually(is("expected value"),
                            RetryConfig.builder()
                                    .timeoutAfter(FIFTY_MILLIS)
                                    .sleepBetweenAttempts(Duration.ofMillis(10))
                                    .retryOnException(false)
                    ));
            fail("expected to fail");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("\nExpected: supplied values to *eventually* match is \"expected value\" within 50ms\n" +
                    "     but: The Supplier threw\n" +
                    "          Actual values (in order of appearance):\n" +
                    "           - thrown java.lang.RuntimeException: Supplier failed"));
        }
    }
}