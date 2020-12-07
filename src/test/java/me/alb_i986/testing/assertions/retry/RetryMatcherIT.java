package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.Suppliers;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RetryMatcherIT {

    @Test
    public void eventuallyPassing() {
        assertThat(Suppliers.ascendingIntegersStartingFrom(4), new RetryMatcher<>(greaterThanOrEqualTo(5)));
    }

    @Test
    public void failingForNoMatchingValue() {
        try {
            assertThat(Suppliers.ascendingIntegersStartingFrom(5), new RetryMatcher<>(lessThan(5)));
            fail("expected to fail");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("\nExpected: supplied values to *eventually* match a value less than <5>\n" +
                    "     but: None of the actual values supplied matched after 2/2 attempts\n" +
                    "    Actual values (in order of appearance):\n" +
                    "         - <5>\n" +
                    "         - <6>"));
        }
    }
}