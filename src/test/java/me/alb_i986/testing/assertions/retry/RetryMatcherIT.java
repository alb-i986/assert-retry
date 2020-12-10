package me.alb_i986.testing.assertions.retry;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RetryMatcherIT {

    @Test
    public void supplierEventuallyReturnsMatchingValue() {
        long startTimeMillis = System.currentTimeMillis();

        assertThat(Suppliers.fromList("a", "b", "c"),
                RetryMatcher.eventually(containsString("c"),
                        RetryConfig.builder()
                                .timeoutAfter(50, TimeUnit.MILLISECONDS)
                                .sleepBetweenAttempts(10, TimeUnit.MILLISECONDS)
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
                                    .timeoutAfter(50, TimeUnit.MILLISECONDS)
                                    .sleepBetweenAttempts(10, TimeUnit.MILLISECONDS)
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
}