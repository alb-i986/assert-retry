package me.alb_i986.testing.assertions.retry;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RetryMatcherIT {

    @Test
    public void supplierEventuallyReturnsMatchingValue() {
        assertThat(Suppliers.ascendingIntegersStartingFrom(4),
                RetryMatcher.eventually(greaterThanOrEqualTo(5),
                        RetryConfig.builder()
                                .maxAttempts(5)
                                .retryOnException(false)
                                .sleepBetweenAttempts(50, TimeUnit.MILLISECONDS)
                ));
    }

    @Test
    public void failingForNoMatchingValue() {
        try {
            assertThat(Suppliers.ascendingIntegersStartingFrom(5),
                    RetryMatcher.eventually(lessThan(5),
                            RetryConfig.builder()
                                    .maxAttempts(3)
                                    .retryOnException(false)
                                    .sleepBetweenAttempts(50, TimeUnit.MILLISECONDS)
                    ));
            fail("expected to fail");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("\nExpected: supplied values to *eventually* match a value less than <5> within 3 attempts\n" +
                    "     but: None of the actual values supplied matched\n" +
                    "          Actual values (in order of appearance):\n" +
                    "           - <5>\n" +
                    "           - <6>\n" +
                    "           - <7>"));
        }
    }
}