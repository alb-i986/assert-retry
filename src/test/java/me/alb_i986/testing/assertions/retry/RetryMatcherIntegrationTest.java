package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.Timeout;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Testing the integration with {@link org.hamcrest.MatcherAssert#assertThat(Object, Matcher)}.
 * <p>
 * Still mocking the clock not to get flaky tests.
 */
public class RetryMatcherIntegrationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Clock clockMock;

    private Timeout timeoutWithMockedClock;

    @Mock
    private Supplier<String> supplierMock;
    @Mock
    private Supplier<List<String>> listSupplierMock;
    @Mock
    private Supplier<String[]> arraySupplierMock;

    private RetryConfigBuilder configBuilder;

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

        configBuilder = RetryConfig.builder()
                .timeout(timeoutWithMockedClock)
                .sleepForMillis(1)
                .retryOnException(false);
    }

    @Test
    public void supplierEventuallyReturnsMatchingValueWithinTimeout() {
        given(supplierMock.get())
                .willReturn("a")
                .willReturn("b")
                .willReturn("c")
                .willReturn("d")
                .willReturn("e")
                .willReturn("f");

        assertThat(supplierMock, RetryMatcher.eventually(
                containsString("e"),
                configBuilder
        ));
    }

    @Test
    public void supplierDoesNotMatchWithinTimeout() {
        given(supplierMock.get())
                .willReturn("a")
                .willReturn("b")
                .willReturn("c")
                .willReturn("d")
                .willReturn("e")
                .willReturn("f");


        try {
            assertThat(supplierMock, RetryMatcher.eventually(
                    containsString("f"),
                    configBuilder
            ));
            fail("expected to fail");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("\nExpected: supplied values to *eventually* match a string containing \"f\" within 49ms\n" +
                    "     but: The timeout was reached and none of the actual values matched\n" +
                    "          Actual values (in order of appearance):\n" +
                    "           - \"a\"\n" +
                    "           - \"b\"\n" +
                    "           - \"c\"\n" +
                    "           - \"d\"\n" +
                    "           - \"e\""));
        }
    }

    @Test
    public void supplierThrowsButEventuallyMatches() {
        given(supplierMock.get())
                .willThrow(new RuntimeException("Supplier failed"))
                .willThrow(new RuntimeException("Supplier failed"))
                .willThrow(new RuntimeException("Supplier failed"))
                .willThrow(new RuntimeException("Supplier failed"))
                .willReturn("found");

        assertThat(supplierMock, RetryMatcher.eventually(is("found"),
                configBuilder
                        .retryOnException(true)
        ));
    }

    @Test
    public void supplierThrowsAndEventuallyDoesNotMatch() {
        given(supplierMock.get())
                .willThrow(new RuntimeException("Supplier failed"))
                .willThrow(new RuntimeException("Supplier failed"))
                .willThrow(new RuntimeException("Supplier failed"))
                .willReturn("never matching actual");

        try {
            assertThat(supplierMock, RetryMatcher.eventually(
                    is("expected value"),
                    configBuilder.retryOnException(true)
            ));
            fail("exception expected");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("\nExpected: supplied values to *eventually* match is \"expected value\" within 49ms\n" +
                    "     but: The timeout was reached and none of the actual values matched\n" +
                    "          Actual values (in order of appearance):\n" +
                    "           - thrown java.lang.RuntimeException: Supplier failed\n" +
                    "           - thrown java.lang.RuntimeException: Supplier failed\n" +
                    "           - thrown java.lang.RuntimeException: Supplier failed\n" +
                    "           - \"never matching actual\"\n" +
                    "           - \"never matching actual\""));
        }
    }

    @Test
    public void supplierThrowsAndRetryOnExceptionIsOff() {
        given(supplierMock.get())
                .willReturn("not expected")
                .willThrow(new RuntimeException("Supplier failed"));

        try {
            assertThat(supplierMock, RetryMatcher.eventually(
                    is("expected value"),
                    configBuilder
            ));
            fail("expected to fail");
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("\nExpected: supplied values to *eventually* match is \"expected value\" within 49ms\n" +
                    "     but: The Supplier threw\n" +
                    "          Actual values (in order of appearance):\n" +
                    "           - \"not expected\"\n" +
                    "           - thrown java.lang.RuntimeException: Supplier failed"));
        }
    }

    // Testing more real life Matcher's...

    @Test
    public void hasSize() {
        given(listSupplierMock.get())
                .willReturn(Arrays.asList("a", "b", "c"))
                .willReturn(Arrays.asList("a"))
                .willReturn(Arrays.asList("b", "a"));

        assertThat(listSupplierMock, RetryMatcher.eventually(
                Matchers.hasSize(2),
                configBuilder
        ));

        verify(listSupplierMock, times(3)).get();
    }

    @Test
    public void containsInAnyOrder() {
        given(listSupplierMock.get())
                .willReturn(Arrays.asList("a", "b", "c"))
                .willReturn(Arrays.asList("a"))
                .willReturn(Arrays.asList("b", "a"));

        assertThat(listSupplierMock, RetryMatcher.eventually(
                Matchers.containsInAnyOrder("a", "b"),
                configBuilder
        ));

        verify(listSupplierMock, times(3)).get();
    }

    @Test
    public void arrayContainingInAnyOrder() {
        given(arraySupplierMock.get())
                .willReturn(new String[]{"a", "b", "c"})
                .willReturn(new String[]{"a"})
                .willReturn(new String[]{"p", "a"});

        assertThat(arraySupplierMock, RetryMatcher.eventually(
                Matchers.arrayContainingInAnyOrder("a", "p"),
                configBuilder
        ));

        verify(arraySupplierMock, times(3)).get();
    }

    @Test
    public void emptyIterable() {
        given(listSupplierMock.get())
                .willReturn(Arrays.asList("a", "b", "c"))
                .willReturn(Arrays.asList("a"))
                .willReturn(Collections.emptyList());

        assertThat(listSupplierMock, RetryMatcher.eventually(
                Matchers.emptyIterable(),
                configBuilder
        ));

        verify(listSupplierMock, times(3)).get();
    }

    @Test
    public void emptyArray() {
        given(arraySupplierMock.get())
                .willReturn(new String[]{"a", "b", "c"})
                .willReturn(new String[]{"a"})
                .willReturn(new String[]{});

        assertThat(arraySupplierMock, RetryMatcher.eventually(
                Matchers.emptyArray(),
                configBuilder
        ));

        verify(arraySupplierMock, times(3)).get();
    }

    @Test
    public void instanceOf() {
        Supplier<Object> supplierMock = Mockito.mock(Supplier.class);
        given(supplierMock.get())
                .willReturn(new Pippo())
                .willReturn(new Pluto())
                .willReturn(new Topolino());

        assertThat(supplierMock, RetryMatcher.eventually(
                Matchers.instanceOf(Topolino.class),
                configBuilder
        ));

        verify(supplierMock, times(3)).get();
    }

    private interface Personaggio {}
    private static class Pippo implements Personaggio {}
    private static class Pluto implements Personaggio {}
    private static class Topolino implements Personaggio {}
}