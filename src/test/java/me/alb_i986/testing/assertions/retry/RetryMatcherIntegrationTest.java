package me.alb_i986.testing.assertions.retry;

import me.alb_i986.testing.assertions.retry.internal.Timeout;
import org.hamcrest.Matcher;
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
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Testing the integration with {@link org.hamcrest.MatcherAssert#assertThat(Object, Matcher)}.
 * <p>
 * Still mocking the clock not to get flaky tests.
 */
public class RetryMatcherIntegrationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Supplier<String> supplierMock;

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

        assertThat(supplierMock, RetryMatcher.eventually(containsString("e"),
                RetryConfig.builder()
                        .timeout(timeoutWithMockedClock)
                        .sleepForMillis(1)
                        .retryOnException(false)
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
            assertThat(supplierMock, RetryMatcher.eventually(containsString("f"),
                    RetryConfig.builder()
                            .timeout(timeoutWithMockedClock)
                            .sleepForMillis(1)
                            .retryOnException(false)
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
                RetryConfig.builder()
                        .timeout(timeoutWithMockedClock)
                        .sleepForMillis(1)
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
            assertThat(supplierMock, RetryMatcher.eventually(is("expected value"),
                    RetryConfig.builder()
                            .timeout(timeoutWithMockedClock)
                            .sleepForMillis(1)
                            .retryOnException(true)
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
            assertThat(supplierMock, RetryMatcher.eventually(is("expected value"),

                    RetryConfig.builder()
                            .timeout(timeoutWithMockedClock)
                            .sleepForMillis(1)
                            .retryOnException(false)
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

    //TODO test more real life Matcher's
}