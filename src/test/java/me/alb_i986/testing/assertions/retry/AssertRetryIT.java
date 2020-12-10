package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.WaitStrategies;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static me.alb_i986.testing.assertions.retry.RetryMatcher.eventually;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AssertRetryIT {

    @Test
    public void supplierEventuallyReturnsMatchingValue() {
        Supplier<String> actual = Suppliers.fromList("a", "b", "c");

        assertThat(actual, eventually(containsString("c"), RetryConfig.builder()
                        .retryOnException(false)
                        .maxAttempts(3)
                        .waitStrategy(WaitStrategies.sleep(2, TimeUnit.SECONDS))
                        // TODO
//                        .sleepBetweenAttempts(5, TimeUnit.SECONDS)
//                        .waitBetweenAttempts(WaitStrategies.sleep(5, TimeUnit.SECONDS))
        ));
    }

    @Test
    public void retryWithTimeout_supplierNeverMatches() {
        try {
            assertThat(Suppliers.id("a"), eventually(containsString("c"), RetryConfig.builder()
                    .timeoutAfter(1, TimeUnit.SECONDS)
                    .waitStrategy(WaitStrategies.sleep(100, TimeUnit.MILLISECONDS))
                    .retryOnException(false)
            ));
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void sleepBetweenAttempts() {
        Supplier<String> actual = Suppliers.fromList("a", "b", "c");
        long startTimeMillis = System.currentTimeMillis();

        // when
        assertThat(actual, eventually(containsString("b"), RetryConfig.builder()
                .sleepBetweenAttempts(1, TimeUnit.SECONDS)
                .maxAttempts(2)
                .retryOnException(false)
        ));

        long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        assertThat(elapsedTimeMillis, allOf(greaterThan(1000L), lessThanOrEqualTo(1300L)));
    }
}