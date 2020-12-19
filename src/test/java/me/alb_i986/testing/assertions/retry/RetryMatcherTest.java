package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.Timeout;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class RetryMatcherTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    // TODO test also logging http://projects.lidalia.org.uk/slf4j-test/

    @Mock
    private WaitStrategy waitStrategyMock;

    @Spy
    private final Supplier<Integer> supplierSpy = TestSuppliers.ascendingIntegersStartingFrom(1);

    @Mock
    private Clock clockMock;

    private Timeout timeoutMock;

    @Before
    public void setUp() {
        given(clockMock.instant())
                .willReturn(Instant.EPOCH)
                .willReturn(Instant.EPOCH.plusMillis(10))
                .willReturn(Instant.EPOCH.plusMillis(20))
                .willReturn(Instant.EPOCH.plusMillis(30))
                .willReturn(Instant.EPOCH.plusMillis(40))
                .willReturn(Instant.EPOCH.plusMillis(50));

        timeoutMock = new Timeout(Duration.ofMillis(49), clockMock);
    }

    @Test
    public void shouldNotRetryWhenSupplierMatchesTheFirstTime() throws Exception {
        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutMock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();
        
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(1), config);

        // when
        assertTrue(sut.matches(supplierSpy));

        verify(supplierSpy, times(1)).get();
        verify(waitStrategyMock, times(0)).waitt();
    }

    @Test
    public void shouldRetryAndEventuallyMatchWhenSupplierMatchesWithinTheTimeout() throws Exception {
        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutMock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();
        
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(3), config);

        // when
        assertTrue(sut.matches(supplierSpy));

        verify(supplierSpy, times(3)).get();
        verify(waitStrategyMock, times(2)).waitt();
    }

    @Test
    public void shouldRetryAndEventuallyNotMatchWhenSupplierDoesNotMatchWithinTimeout() throws Exception {
        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutMock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();
       
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(6), config);

        // when
        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, times(5)).get();
        verify(waitStrategyMock, times(4)).waitt();
    }

    @Test
    public void shouldNotRetryWhenSupplierThrowsGivenRetryOnExceptionIsOff() throws Exception {
        Supplier<String> supplierSpy = Mockito.spy(TestSuppliers.throwing());

        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutMock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();

        RetryMatcher<String> sut = new RetryMatcher<>(is("WHATEVER"), config);

        // when
        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, times(1)).get();
        verify(waitStrategyMock, times(0)).waitt();
    }

    @Test
    public void shouldRetryWhenSupplierThrowsGivenRetryOnExceptionIsOn() throws Exception {
        Supplier<String> supplierSpy = Mockito.spy(TestSuppliers.throwing());

        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutMock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(true)
                .build();

        RetryMatcher<String> sut = new RetryMatcher<>(is("WHATEVER"), config);

        // when
        assertFalse(sut.matches(supplierSpy));

        verify(supplierSpy, times(5)).get();
        verify(waitStrategyMock, times(4)).waitt();
    }

    @Test
    public void shouldNotFailMiserablyWhenTheWaitStrategyThrows() throws Exception {
        willThrow(new RuntimeException("dummy exception")).given(waitStrategyMock).waitt();

        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutMock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();

        RetryMatcher<Integer> sut = new RetryMatcher<>(is(2), config);

        assertTrue(sut.matches(supplierSpy));

        verify(waitStrategyMock, times(1)).waitt();
    }
}