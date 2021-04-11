package me.alb_i986.retry.hamcrest;

import me.alb_i986.retry.testutils.MyException;
import me.alb_i986.retry.testutils.SubException;
import me.alb_i986.retry.testutils.SuperException;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;

/**
 * Testing end-to-end the integration between
 * {@link org.hamcrest.MatcherAssert#assertThat(Object, Matcher)}
 * and our {@link RetryMatcher}.
 * <p>
 * Mocking the Supplier only.
 */
public class RetryMatcherE2eTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    
    @Mock
    private Supplier<String> supplierMock;
    @Mock
    private Supplier<List<String>> listSupplierMock;
    @Mock
    private Supplier<String[]> arraySupplierMock;
    @Mock
    private Supplier<Animal> animalSupplierMock;
    
    @Test
    public void matchesWithinTimeout() {
        given(supplierMock.get())
                .willReturn("one")
                .willReturn("two")
                .willThrow(new RuntimeException("unreachable"));

        assertThat(supplierMock, RetryMatcher.eventually(is("two"))
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .build());
    }

    @Test
    public void timeoutExpires() {
        given(supplierMock.get())
                .willReturn("one")
                .willReturn("two")
                .willReturn("three")
                .willReturn("four");

        assertThatThrownBy(() ->
                assertThat(supplierMock, RetryMatcher.eventually(is("four"))
                        .within(100, ChronoUnit.MILLIS)
                        .sleepForMillis(90)
                        .build()))
                .isInstanceOf(AssertionError.class)
                .hasMessage("\nExpected: supplied value to *eventually* match a string containing \"thre\" within 100ms\n" +
                        "     but: The timeout expired and none of the actual values matched\n" +
                        "          Actual results (in order of appearance):\n" +
                        "           1. \"one\"\n" +
                        "           2. \"two\"\n" +
                        "           3. \"three\"");
    }

    @Test
    public void retryOnExceptionAndEventuallyMatches() {
        given(supplierMock.get())
                .willThrow(new SubException("simulated"))
                .willReturn("one")
                .willThrow(new RuntimeException("unreachable"));

        assertThat(supplierMock, RetryMatcher.eventually(is("one"))
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .retryOnException(SuperException.class)
                .build());
    }

    @Test
    public void retryOnExceptionButThrowsSuperException() {
        given(supplierMock.get())
                .willThrow(new SuperException("simulated"))
                .willReturn("one");

        assertThatThrownBy(() ->
                assertThat(supplierMock, RetryMatcher.eventually(is("one"))
                        .within(100, ChronoUnit.MILLIS)
                        .sleepForMillis(10)
                        .retryOnException(SubException.class)
                        .build()))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("\nExpected: supplied value to *eventually* match is \"one\" within 100ms\n" +
                        "     but: an exception was thrown while retrieving the actual value\n" +
                        "          Actual results (in order of appearance):\n" +
                        "           1. thrown me.alb_i986.retry.testutils.SuperException: simulated\n" +
                        "\tat me.alb_i986.retry.AssertRetry.doAssert");
    }

    @Test
    public void retryOnExceptionNotConfigured() {
        given(supplierMock.get())
                .willThrow(new MyException("simulated"))
                .willReturn("one");

        assertThatThrownBy(() ->
                assertThat(supplierMock, RetryMatcher.eventually(is("one"))
                        .within(100, ChronoUnit.MILLIS)
                        .sleepForMillis(10)
                        // retry on exception is NOT configured
                        .build()))
                .isInstanceOf(AssertionError.class)
                .hasMessageStartingWith("\nExpected: supplied value to *eventually* match is \"one\" within 100ms\n" +
                        "     but: an exception was thrown while retrieving the actual value\n" +
                        "          Actual results (in order of appearance):\n" +
                        "           1. thrown me.alb_i986.retry.testutils.MyException: simulated\n" +
                        "\tat me.alb_i986.retry.AssertRetry.doAssert");
    }
    
    // Now let's test it with some real life Matcher's

    @Test
    public void containsString() {
        given(supplierMock.get())
                .willReturn("one")
                .willReturn("two")
                .willThrow(new RuntimeException("unreachable"));

        assertThat(supplierMock, RetryMatcher.eventually(Matchers.containsString("wo"))
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .build());
    }

    @Test
    public void hasLength() {
        given(supplierMock.get())
                .willReturn("a")
                .willReturn("aa")
                .willThrow(new RuntimeException("unreachable"));

        assertThat(supplierMock, RetryMatcher.eventually(Matchers.hasLength(2))
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .build());
    }

    @Test
    public void collectionHasSize() {
        given(listSupplierMock.get())
                .willReturn(asList("a", "b"))
                .willReturn(asList("a", "b", "c"))
                .willThrow(new RuntimeException("unreachable"));

        assertThat(listSupplierMock, RetryMatcher.eventually(Matchers.hasSize(3))
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .build());
    }

    @Test
    public void iterableContainsInAnyOrder() {
        given(listSupplierMock.get())
                .willReturn(asList("a", "b", "c"))
                .willReturn(asList("a", "b"))
                .willThrow(new RuntimeException("unreachable"));

        assertThat(listSupplierMock, RetryMatcher.eventually(Matchers.containsInAnyOrder("b", "a"))
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .build());
    }

    @Test
    public void arrayContainingInAnyOrder() {
        given(arraySupplierMock.get())
                .willReturn(new String[]{"a", "b", "c"})
                .willReturn(new String[]{"a", "b"})
                .willThrow(new RuntimeException("unreachable"));

        assertThat(arraySupplierMock, RetryMatcher.eventually(Matchers.arrayContainingInAnyOrder("b", "a"))
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .build());
    }

    @Test
    public void emptyIterable() {
        given(listSupplierMock.get())
                .willReturn(asList("a", "b"))
                .willReturn(Collections.emptyList())
                .willThrow(new RuntimeException("unreachable"));

        assertThat(listSupplierMock, RetryMatcher.eventually(Matchers.emptyIterable())
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .build());
    }

    @Test
    public void emptyArray() {
        given(arraySupplierMock.get())
                .willReturn(new String[]{"a", "b"})
                .willReturn(new String[]{})
                .willThrow(new RuntimeException("unreachable"));

        assertThat(arraySupplierMock, RetryMatcher.eventually(Matchers.emptyArray())
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .build());
    }

    @Test
    public void instanceOf() {
        given(animalSupplierMock.get())
                .willReturn(new Cat())
                .willReturn(new Dog())
                .willThrow(new RuntimeException("unreachable"));

        assertThat(animalSupplierMock, RetryMatcher.eventually(Matchers.instanceOf(Dog.class))
                .within(100, ChronoUnit.MILLIS)
                .sleepForMillis(10)
                .build());
    }

    private interface Animal {
    }

    private static class Cat implements Animal {
    }

    private static class Dog implements Animal {
    }
}
