package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.Timeout;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
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

public class RetryMatcherTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    // TODO test also logging http://projects.lidalia.org.uk/slf4j-test/

    @Mock
    private WaitStrategy waitStrategyMock;

    @Mock
    private Supplier<Integer> supplierMock;

    @Mock
    private Clock clockMock;

    private Timeout timeoutWithMockedClock;

    @Before
    public void setUp() {
        given(clockMock.instant())
                .willReturn(Instant.EPOCH)
                .willReturn(Instant.EPOCH.plusMillis(10))
                .willReturn(Instant.EPOCH.plusMillis(20))
                .willReturn(Instant.EPOCH.plusMillis(30))
                .willReturn(Instant.EPOCH.plusMillis(40))
                .willReturn(Instant.EPOCH.plusMillis(50));

        timeoutWithMockedClock = new Timeout(Duration.ofMillis(49), clockMock);

        given(supplierMock.get())
                .willReturn(1)
                .willReturn(2)
                .willReturn(3)
                .willReturn(4)
                .willReturn(5)
                .willReturn(6);
    }

    @Test
    public void shouldNotRetryWhenSupplierMatchesTheFirstTime() throws Exception {
        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutWithMockedClock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();
        
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(1), config);

        // when
        assertTrue(sut.matches(supplierMock));

        verify(supplierMock, times(1)).get();
        verify(waitStrategyMock, times(0)).waitt();
    }

    @Test
    public void shouldRetryAndEventuallyMatchWhenSupplierMatchesWithinTheTimeout() throws Exception {
        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutWithMockedClock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();
        
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(5), config);

        // when
        assertTrue(sut.matches(supplierMock));

        verify(supplierMock, times(5)).get();
        verify(waitStrategyMock, times(4)).waitt();
    }

    @Test
    public void shouldRetryAndEventuallyNotMatchWhenSupplierDoesNotMatchWithinTimeout() throws Exception {
        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutWithMockedClock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();
       
        RetryMatcher<Integer> sut = new RetryMatcher<>(is(6), config);

        // when
        assertFalse(sut.matches(supplierMock));

        verify(supplierMock, times(5)).get();
        verify(waitStrategyMock, times(4)).waitt();
    }

    @Test
    public void shouldNotRetryWhenSupplierThrowsGivenRetryOnExceptionIsOff() throws Exception {
        given(supplierMock.get())
                .willThrow(new RuntimeException("Supplier failed"));

        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutWithMockedClock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();

        RetryMatcher<String> sut = new RetryMatcher<>(is("WHATEVER"), config);

        // when
        assertFalse(sut.matches(supplierMock));

        verify(supplierMock, times(1)).get();
        verify(waitStrategyMock, times(0)).waitt();
    }

    @Test
    public void shouldRetryWhenSupplierThrowsGivenRetryOnExceptionIsOn() throws Exception {
        given(supplierMock.get())
                .willThrow(new RuntimeException("Supplier failed"));

        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutWithMockedClock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(true)
                .build();

        RetryMatcher<String> sut = new RetryMatcher<>(is("WHATEVER"), config);

        // when
        assertFalse(sut.matches(supplierMock));

        verify(supplierMock, times(5)).get();
        verify(waitStrategyMock, times(4)).waitt();
    }

    @Test
    public void shouldNotFailMiserablyWhenTheWaitStrategyThrows() throws Exception {
        willThrow(new RuntimeException("dummy exception")).given(waitStrategyMock).waitt();

        RetryConfig config = new RetryConfigBuilder()
                .timeout(timeoutWithMockedClock)
                .waitStrategy(waitStrategyMock)
                .retryOnException(false)
                .build();

        RetryMatcher<Integer> sut = new RetryMatcher<>(is(2), config);

        assertTrue(sut.matches(supplierMock));

        verify(waitStrategyMock, times(1)).waitt();
    }
}