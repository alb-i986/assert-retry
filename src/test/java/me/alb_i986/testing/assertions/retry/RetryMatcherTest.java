package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.WaitStrategies;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.Duration;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class RetryMatcherTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    // TODO test also logging http://projects.lidalia.org.uk/slf4j-test/

    @Spy
    private Runnable waitStrategySpy = WaitStrategies.sleep(10);

    @Spy
    private final Supplier<Integer> supplierSpy = Suppliers.ascendingIntegersStartingFrom(1);

    @Test
    public void shouldNotRetryWhenSupplierMatchesTheFirstTime() {
        RetryConfig config = new RetryConfigBuilder()
                .timeoutAfter(Duration.ofMillis(500))
                .waitStrategy(waitStrategySpy)
                .retryOnException(false)
                .build();
        
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(1), config);

        // when
        assertTrue(sut.matches(supplierSpy));

        verify(supplierSpy, times(1)).get();
        verify(waitStrategySpy, times(0)).run();
    }

    @Test
    public void shouldRetryAndEventuallyMatchWhenSupplierMatchesWithinTheTimeout() {
        RetryConfig config = new RetryConfigBuilder()
                .timeoutAfter(Duration.ofMillis(500))
                .waitStrategy(waitStrategySpy)
                .retryOnException(false)
                .build();
        
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(3), config);

        // when
        assertTrue(sut.matches(supplierSpy));

        verify(supplierSpy, times(3)).get();
        verify(waitStrategySpy, times(2)).run();
    }

    @Test
    public void shouldRetryAndEventuallyNotMatchWhenSupplierDoesNotMatchWithinTimeout() {
        RetryConfig config = new RetryConfigBuilder()
                .timeoutAfter(Duration.ofMillis(100))
                .waitStrategy(waitStrategySpy)
                .retryOnException(false)
                .build();
       
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(11), config);

        // when
        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, atLeast(10)).get();
        verify(supplierSpy, atMost(11)).get();
        verify(waitStrategySpy, times(10)).run();
    }

    @Test
    public void shouldNotRetryWhenSupplierThrowsGivenRetryOnExceptionIsOff() {
        Supplier<String> supplierSpy = Mockito.spy(Suppliers.throwing());
        
        RetryConfig config = new RetryConfigBuilder()
                .timeoutAfter(Duration.ofMillis(100))
                .waitStrategy(waitStrategySpy)
                .retryOnException(false)
                .build();
        
        RetryMatcher<String> sut = new RetryMatcher<>(is("WHATEVER"), config);

        // when
        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, times(1)).get();
        verify(waitStrategySpy, times(0)).run();
    }

    @Test
    public void shouldRetryWhenSupplierThrowsGivenRetryOnExceptionIsOn() {
        Supplier<String> supplierSpy = Mockito.spy(Suppliers.throwing());

        RetryConfig config = new RetryConfigBuilder()
                .timeoutAfter(Duration.ofMillis(100))
                .waitStrategy(waitStrategySpy)
                .retryOnException(true)
                .build();

        RetryMatcher<String> sut = new RetryMatcher<>(is("WHATEVER"), config);

        // when
        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, atLeast(9)).get();
        verify(supplierSpy, atMost(11)).get();
        verify(waitStrategySpy, times(9)).run();
    }

    @Test
    public void shouldNotFailMiserablyWhenTheWaitStrategyThrows() {
        RetryConfig config = new RetryConfigBuilder()
                .timeoutAfter(Duration.ofMillis(100))
                .waitStrategy(() -> {throw new RuntimeException("asd");})
                .retryOnException(false)
                .build();

        RetryMatcher<Integer> sut = new RetryMatcher<>(is(5), config);

        assertTrue(sut.matches(supplierSpy));
    }
}