package me.alb_i986.retry;

import me.alb_i986.retry.wait.WaitStrategy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.*;

public class AssertRetryTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Timeout timeoutMock;
    @Mock
    private WaitStrategy waitStrategyMock;
    @Mock
    private RetryOnException retryOnExceptionMock;
    @Mock
    private Supplier<Integer> supplierMock;
    @Mock
    private Logger loggerMock;

    private AssertRetry<Integer> sut;

    @Before
    public void setUp() {
        RetryConfig config = new RetryConfig(timeoutMock, waitStrategyMock, retryOnExceptionMock);
        sut = new AssertRetry<>(config, loggerMock);
    }

    @Test
    public void shouldNotRetryWhenSupplierMatchesTheFirstTime() {
        given(supplierMock.get())
                .willReturn(1);
        given(timeoutMock.isExpired())
                .willReturn(false);

        sut.doAssert(supplierMock, n -> true);

        verify(supplierMock, times(1)).get();
        verify(waitStrategyMock, never()).runWait();
        verify(timeoutMock).start();
    }

    @Test
    public void shouldRetryAndMatchWhenSupplierMatchesWithinTheTimeout() {
        given(supplierMock.get())
                .willReturn(1)
                .willReturn(2);
        given(timeoutMock.isExpired())
                .willReturn(false);

        sut.doAssert(supplierMock, n -> n == 2);

        verify(supplierMock, times(2)).get();
        verify(waitStrategyMock).runWait();
        verify(timeoutMock).start();
    }

    @Test
    public void shouldThrowWhenNotMatchingAndTimeoutExpires() {
        given(supplierMock.get())
                .willReturn(1)
                .willReturn(2);
        given(timeoutMock.isExpired())
                .willReturn(false)
                .willReturn(true);
        given(timeoutMock.getDuration())
                .willReturn(Duration.ofSeconds(5));

        AssertRetryTimeoutError e =
                assertThrows(AssertRetryTimeoutError.class, () ->
                        sut.doAssert(supplierMock, n -> false));

        verify(supplierMock, times(2)).get();
        verify(waitStrategyMock).runWait();

        assertThat(e.getMessage(), equalTo("Retry assertion failed after 2 attempts: The timeout expired and none of the actual values matched"));
    }

    @Test
    public void shouldThrowWhenSupplierThrowsAndRetryOnExceptionIsOff() {
        given(supplierMock.get())
                .willThrow(new RuntimeException("dummy"));

        SupplierThrewError e =
                assertThrows(SupplierThrewError.class, () ->
                        sut.doAssert(supplierMock, n -> false));

        verify(supplierMock).get();
        verify(waitStrategyMock, never()).runWait();

        assertThat(e.getMessage(), equalTo("Retry assertion failed after 1 attempt: " +
                "an exception was thrown while retrieving the actual value"));
        assertNotNull(e.getCause());
    }

    @Test
    public void shouldRetryWhenSupplierThrowsCompatibleException() {
        given(supplierMock.get())
                .willThrow(new RuntimeException("dummy"))
                .willReturn(2);

        given(retryOnExceptionMock.isOn())
                .willReturn(true);
        given(retryOnExceptionMock.matches(Mockito.any(Exception.class)))
                .willReturn(true);

        sut.doAssert(supplierMock, n -> n == 2);

        verify(supplierMock, times(2)).get();
        verify(waitStrategyMock).runWait();
    }

    @Test
    public void shouldThrowWhenSupplierThrowsNonCompatibleException() {
        given(supplierMock.get())
                .willThrow(new RuntimeException("dummy"));

        given(retryOnExceptionMock.isOn())
                .willReturn(true);
        given(retryOnExceptionMock.matches(Mockito.any(Exception.class)))
                .willReturn(false);

        SupplierThrewError e =
                assertThrows(SupplierThrewError.class, () ->
                        sut.doAssert(supplierMock, n -> true));

        verify(supplierMock).get();
        verify(waitStrategyMock, never()).runWait();

        assertThat(e.getMessage(), equalTo("Retry assertion failed after 1 attempt:" +
                " an exception was thrown while retrieving the actual value"));
        assertNotNull(e.getCause());
    }

    @Test
    public void shouldNotFailMiserablyWhenWaitStrategyThrows() {
        given(supplierMock.get())
                .willReturn(1)
                .willReturn(2);

        willThrow(new RuntimeException("dummy"))
                .given(waitStrategyMock).runWait();
        given(waitStrategyMock.getDescription())
                .willReturn("mocked description");

        sut.doAssert(supplierMock, n -> n == 2);

        verify(waitStrategyMock).runWait();
//        verify(loggerMock).debug("mocked description"); //TODO also test logging
    }
}