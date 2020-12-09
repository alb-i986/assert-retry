package me.alb_i986.testing.assertions.retry;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RetryMatcherTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    // TODO test also logging http://projects.lidalia.org.uk/slf4j-test/

    private final RetryConfigBuilder baseConfigBuilder = new RetryConfigBuilder()
            .retryOnException(false)
            .sleepBetweenAttempts(10, TimeUnit.MILLISECONDS);

    @Spy
    private final Supplier<Integer> supplierSpy = Suppliers.ascendingIntegersStartingFrom(1);

    @Test
    public void shouldNotRetryWhenSupplierMatchesTheFirstTime() {
        RetryConfig config = baseConfigBuilder.maxAttempts(3).build();
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(1), config);

        assertTrue(sut.matches(supplierSpy));

        verify(supplierSpy, times(1)).get();
    }

    @Test
    public void shouldRetryAndEventuallyMatchWhenSupplierMatchesWithinMaxAttempts() {
        RetryConfig config = baseConfigBuilder.maxAttempts(3).build();
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(3), config);

        assertTrue(sut.matches(supplierSpy));

        verify(supplierSpy, times(3)).get();
    }

    @Test
    public void shouldRetryAndEventuallyMatchWhenSupplierMatchesWithinTheTimeout() {
        RetryConfig config = baseConfigBuilder
                .timeoutAfter(1, TimeUnit.SECONDS)
                .build();
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(3), config);

        assertTrue(sut.matches(supplierSpy));

        verify(supplierSpy, times(3)).get();
    }

    @Test
    public void shouldRetryAndEventuallyNotMatchWhenSupplierDoesNotMatchWithinMaxAttempts() {
        RetryConfig config = baseConfigBuilder.maxAttempts(3).build();
        RetryMatcher sut = new RetryMatcher<>(is(4), config);

        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, times(3)).get();
    }

    @Test
    public void shouldRetryAndEventuallyNotMatchWhenSupplierDoesNotMatchWithinTimeout() {
        RetryConfig config = baseConfigBuilder
                .timeoutAfter(100, TimeUnit.MILLISECONDS)
                .sleepBetweenAttempts(50, TimeUnit.MILLISECONDS)
                .build();
        RetryMatcher sut = new RetryMatcher<>(is(4), config);

        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, times(3)).get();
    }

    @Test
    public void shouldNotRetryWhenSupplierThrowsGivenConfigNotToRetryOnException() {
        Supplier<String> supplierSpy = Mockito.spy(Suppliers.throwing());
        RetryConfig config = baseConfigBuilder
                .retryOnException(false)
                .maxAttempts(3)
                .build();
        RetryMatcher<String> sut = new RetryMatcher<>(is("WHATEVER"), config);

        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, times(1)).get();
    }

    @Test
    public void shouldRetryWhenSupplierThrowsGivenConfigRetryOnException() {
        Supplier<String> supplierSpy = Mockito.spy(Suppliers.throwing());
        RetryConfig config = baseConfigBuilder
                .retryOnException(true)
                .maxAttempts(3)
                .build();
        RetryMatcher<String> sut = new RetryMatcher<>(is("WHATEVER"), config);

        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, times(Math.toIntExact(config.getMaxAttempts()))).get();
    }
}